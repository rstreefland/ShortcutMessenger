package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.operation.user.LoginUserOperation;
import uk.co.streefland.rhys.finalyearproject.operation.user.RegisterUserOperation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the set of user objects stored on the local node and the operations to manage them
 */
public class Users implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private transient LocalNode localNode;

    private User localUser;
    private final List<User> users;

    public Users(LocalNode localNode) {
        this.users = new ArrayList<>();
        initialise(localNode);
    }

    /**
     * Initialise the class with the LocalNode object
     *
     * @param localNode
     */
    public void initialise(LocalNode localNode) {
        this.localNode = localNode;
    }

    /**
     * Invokes the register user operation to register the user on the network and returns whether it was successful
     *
     * @param user The user object to register on the network
     * @return True if the user was registered successfully
     * @throws IOException
     */
    public boolean registerUser(User user) throws IOException {
        user.setRegisterTime(); // set the register time to now
        user.addAssociatedNode(localNode.getNode()); // add the local node as an associated node

        RegisterUserOperation ruo = new RegisterUserOperation(localNode, user);
        ruo.execute();

        /* Set the local user object */
        if (ruo.isRegisteredSuccessfully()) {
            localUser = user; // set the local user object for future reference
        }

        return ruo.isRegisteredSuccessfully();
    }

    /**
     * Invokes the login user operation to log an existing user into the network on this node
     *
     * @param user
     * @param plainTextPassword
     * @return
     * @throws IOException
     */
    public boolean loginUser(User user, String plainTextPassword) throws IOException {
        LoginUserOperation luo = new LoginUserOperation(localNode, user, plainTextPassword);
        luo.execute();

        /* Update the rest of the nodes on the network with the last login time */
        if (luo.isLoggedIn()) {
            user.setLastLoginTime(); // set the last login time
            user.addAssociatedNode(localNode.getNode()); // add the local node as the associated node

            /* Run the RegisterUserOperation to update other nodes on the network with the new user object*/
            RegisterUserOperation ruo = new RegisterUserOperation(localNode, user);
            ruo.execute();

            localUser = user; // set the local user object for future reference
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
        for (User user : users) {
            if (user.getUserName().equals(newUser.getUserName())) {
                /* For login */
                if ((user.getLastLoginTime() < newUser.getLastLoginTime()) && newUser.getRegisterTime() == 0L) {
                    logger.debug("New user object has newer login timestamp - replacing the old one");
                    users.remove(user);
                    users.add(newUser);
                    return false;
                }
                /* If new user has an older registration time that isn't zero - replace the existing user */
                if (user.getRegisterTime() > newUser.getRegisterTime() && newUser.getRegisterTime() == 0L) {
                    logger.debug("New user has an older registration timestamp - replacing the old one");
                    users.remove(user);
                    users.add(newUser);
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
        }
        /* User doesn't exist in out memory - add it */
        users.add(newUser);
        logger.debug("New user added to users: {}", newUser.getUserName());
        return true;
    }

    /**
     * Looks for a user with a specified userName on the local node and returns the user object if it was found
     *
     * @param userName
     * @return
     */
    public synchronized User findUser(String userName) {
        for (User user : users) {
            if (user.getUserName().equals(userName)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Looks for a user with a specified userId on the local node and returns the user object if it was found
     *
     * @param newUser
     * @return The user object that was found
     */
    public synchronized User matchUser(User newUser) {
        for (User user : users) {
            if (user.getUserId().equals(newUser.getUserId())) {
                logger.debug("Found a matching user");
                return user;
            }
        }
        return null;
    }

    public User getLocalUser() {
        return localUser;
    }

    public List<User> getUsers() {
        return users;
    }
}
