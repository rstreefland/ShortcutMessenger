package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.operation.user.FindUserOperation;
import uk.co.streefland.rhys.finalyearproject.operation.user.LoginUserOperation;
import uk.co.streefland.rhys.finalyearproject.operation.user.RegisterUserOperation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the set of user objects stored on the local node and the operations to manage them
 */
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final HashMap<String, User> users;
    private final HashMap<String, User> cache;
    private transient LocalNode localNode;
    private User localUser;
    private String localUserPassword;

    public Users(LocalNode localNode) {
        this.users = new HashMap<>();
        this.cache = new HashMap<>();
        init(localNode);
    }

    /**
     * Used to set the transient objects when a saved state has been read
     */
    public void init(LocalNode localNode) {
        this.localNode = localNode;
    }

    /**
     * Invokes the register user operation to register the user on the network and returns whether it was successful
     *
     * @param userName The user object to register on the network
     * @return True if the user was registered successfully
     * @throws IOException
     */
    public boolean registerUser(String userName, String plainTextPassword) throws IOException {
        /* Create and prepare the user object */
        User user = new User(userName, plainTextPassword);
        user.addAssociatedNode(localNode.getNode()); // add the local node as an associated node
        user.setRegisterTime();

        /* Invoke RegisterUserOperation */
        RegisterUserOperation ruo = new RegisterUserOperation(localNode, user, true);
        ruo.execute();

        /* Set the local user object */
        if (ruo.isRegisteredSuccessfully()) {
            localUserPassword = plainTextPassword;
            localUser = user; // set the local user object
            users.put(userName, user); // add the local user to the list of users
        }
        return ruo.isRegisteredSuccessfully();
    }

    /**
     * Invokes the login user operation to log an existing user into the network on this node
     *
     * @param userName
     * @param plainTextPassword
     * @return
     * @throws IOException
     */
    public boolean loginUser(String userName, String plainTextPassword) throws IOException {

        /* Create the user object */
        User user = new User(userName, plainTextPassword);

        /* Invoke LoginUserOperation */
        LoginUserOperation luo = new LoginUserOperation(localNode, user, plainTextPassword);
        luo.execute();

        /* Update the rest of the nodes on the network with the last login time */
        if (luo.isLoggedIn()) {
            user.setLastActiveTime(); // set the last login time
            user.addAssociatedNode(localNode.getNode()); // add the local node as the associated node

            /* Run the RegisterUserOperation to update other nodes on the network with the new user object*/
            RegisterUserOperation ruo = new RegisterUserOperation(localNode, user, true);
            ruo.execute();

            localUserPassword = plainTextPassword;
            localUser = user; // set the local user object
            users.put(userName, user); // add the user to the list of users
        }
        return luo.isLoggedIn();
    }

    /**
     * Stores a user on the local node
     * Replaces the existing one based on the logic in the comments
     *
     * @param newUser The user to store on the local node
     * @return False if the user already exists on the network
     */
    public synchronized boolean addUser(User newUser) {

        User user = users.get(newUser.getUserName());

        if (user != null) {
            if ((user.getLastActiveTime() < newUser.getLastActiveTime()) && newUser.getRegisterTime() != 0L) {
                logger.debug("New user object has newer last active timestamp - replacing the old one");
                replaceUser(user, newUser);
                return false;
            }

            /* If new user has an older registration time that isn't zero - replace the existing user */
            if (user.getRegisterTime() > newUser.getRegisterTime() && newUser.getRegisterTime() != 0L) {
                logger.debug("New user has an older registration timestamp - replacing the old one");
                replaceUser(user, newUser);
                return false;
            }

            /* If existing user has older registration time then do nothing */
            if ((user.getRegisterTime() < newUser.getRegisterTime())) {
                logger.debug("User with the same name exists - discarding");
                return false;
            }

            /* Users are exactly the same - do nothing */
            if (user.getRegisterTime() == newUser.getRegisterTime()) {
                logger.debug("User objects are the same, doing nothing");
                return false;
            }
        }

        /* User doesn't exist in our memory - add it */
        users.put(newUser.getUserName(), newUser);
        logger.debug("New user added to users: {}", newUser.getUserName());
        return true;
    }

    /**
     * Replaces a user and updates the routing table if required
     *
     * @param oldUser The user object to replace
     * @param newUser The new user object
     */
    private void replaceUser(User oldUser, User newUser) {

        users.replace(newUser.getUserName(), oldUser, newUser);

        /* Update the associated node if it's present in the local routing table */
        if (!oldUser.getAssociatedNode().equals(newUser.getAssociatedNode())) {
            localNode.getRoutingTable().setUnresponsiveContact(oldUser.getAssociatedNode());
            localNode.getRoutingTable().insert(newUser.getAssociatedNode());
        }
    }

    /**
     * Adds a user object to the cache and updates the routing table if required
     *
     * @param newUser
     * @param resetNodeStaleCount
     */
    public synchronized void addUserToCache(User newUser, boolean resetNodeStaleCount) {

        /* Update the associated node if it's present in the local routing table */
        localNode.getRoutingTable().refreshContact(newUser.getAssociatedNode(), resetNodeStaleCount);

        User oldUser = users.get(newUser.getUserName());

        /* User object is present in users therefore we don't need to bother with the cache */
        if (oldUser != null) {
            users.replace(newUser.getUserName(), oldUser, newUser);
            return;
        }

        /* Replace the user object in the cache */
        cache.remove(newUser.getUserName());
        cache.put(newUser.getUserName(), newUser);
    }

    /**
     * Looks for a user with a specified userName on the local node and returns the user object if it was found
     *
     * @param userName
     * @return
     */
    public synchronized User findUser(String userName) {

        /* Look in users first */
        User user = users.get(userName);
        if (user != null) {
            return user;
        }

        /* Look in cache if not found in users */
        user = cache.get(userName);
        return user;
    }

    public synchronized User findUser(User user) {
        return findUser(user.getUserName());
    }

    /**
     * Looks for a user object on the network
     *
     * @param userName The username to search for
     * @return The user object or null if the user couldn't be found
     * @throws IOException
     */
    public synchronized User findUserOnNetwork(String userName) throws IOException {

        /* Look on the local node first */
        User user = findUser(userName);

        /* Look on the network if not found locally */
        if (findUser(userName) == null) {
            FindUserOperation fuo = new FindUserOperation(localNode, new User(userName, ""));
            fuo.execute();
            user = fuo.getFoundUser();
            if (user != null) {
                addUserToCache(user, false);
            }
        }

        return user;
    }

    /**
     * Cleans up cached user objects that are older than the value specified in Configuration.USER_CACHE_EXPIRY
     */
    public synchronized void cleanUp() {
        long currentTime = new Date().getTime() / 1000; // current time in seconds

        for (Map.Entry<String, User> entry : cache.entrySet()) {
            if (currentTime >= entry.getValue().getLastActiveTime() + Configuration.USER_CACHE_EXPIRY) {
                cache.remove(entry.getKey());
            }
        }
    }

    @Override
    public synchronized final String toString() {
        StringBuilder sb = new StringBuilder("\n****** Users ******");

        sb.append("\nUsers on local node: ");

        for (Map.Entry user : users.entrySet()) {
            sb.append("\n");
            sb.append(user.getValue().toString());
        }

        sb.append("\nUsers in cache: ");

        for (Map.Entry user : cache.entrySet()) {
            sb.append("\n");
            sb.append(user.getValue().toString());
        }

        sb.append("\n");
        sb.append("****** Users Ended ******\n");
        return sb.toString();
    }

    public User getLocalUser() {
        return localUser;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public String getLocalUserPassword() {
        return localUserPassword;
    }
}
