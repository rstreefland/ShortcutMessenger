package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.operation.LoginUserOperation;
import uk.co.streefland.rhys.finalyearproject.operation.RegisterUserOperation;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the set of user objects stored on the local node and the operations to manage them
 */
public class Users implements Serializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private User localUser;
    private List<User> users;

    private transient Server server;
    private transient LocalNode localNode;
    private transient Configuration config;

    public Users(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;

        this.users = new ArrayList<>();
    }

    /**
     * Invokes the register user operation to register the user on the network and returns whether it was successful
     *
     * @param user The user object to register on the network
     * @return True if the user was registered successfully
     * @throws IOException
     */
    public boolean registerUser(User user) throws IOException {
        user.setRegisterTime(); // set the register time
        user.addAssociatedNode(localNode.getNode()); // add the local node as an associated node

        RegisterUserOperation operation = new RegisterUserOperation(server, localNode, config, user);
        operation.execute();

        /* Set the local user object */
        if (operation.isRegisteredSuccessfully()) {
            localUser = user; // set the local user object for future reference
        }

        return operation.isRegisteredSuccessfully();
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
        LoginUserOperation operation = new LoginUserOperation(server, localNode, config, user, plainTextPassword);
        operation.execute();

        /* Update the rest of the nodes on the network with the last login time */
        if (operation.isLoggedIn()) {
            user.setLastLoginTime(); // set the last login time
            user.addAssociatedNode(localNode.getNode());

            RegisterUserOperation registerOperation = new RegisterUserOperation(server, localNode, config, user);
            registerOperation.execute();
            localUser = user; // set the local user object for future reference
        }
        return operation.isLoggedIn();
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

        /* User doest't exist in out memory --add it */
        users.add(newUser);
        logger.debug("New user added to users: {}", newUser.getUserName());
        return true;
    }

    /**
     * Looks for a user on the local node and returns the object if it was found
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

    public void updateAfterLoad(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;
    }

    public User getLocalUser() {
        return localUser;
    }

    public void setLocalUser(User localUser) {
        this.localUser = localUser;
    }

    public List<User> getUsers() {
        return users;
    }
}
