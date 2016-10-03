package uk.co.streefland.rhys.finalyearproject.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.streefland.rhys.finalyearproject.operation.LoginUserOperation;
import uk.co.streefland.rhys.finalyearproject.operation.RegisterUserOperation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *  Represents the set of user objects stored on the local node and the operations to manage them
 */
public class Users {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private User localUser;
    private List<User> users;

    private final Server server;
    private final LocalNode localNode;
    private final Configuration config;

    public Users(Server server, LocalNode localNode, Configuration config) {
        this.server = server;
        this.localNode = localNode;
        this.config = config;

        this.users = new ArrayList<>();
    }

    /**
     * Invokes the register user operation to register the user on the network and returns whether it was successful
     * @param user The user object to register on the network
     * @return True if the user was registered successfully
     * @throws IOException
     */
    public boolean registerUser(User user) throws IOException {
        RegisterUserOperation operation = new RegisterUserOperation(server, localNode, config, user);
        operation.execute();
        return operation.isRegisteredSuccessfully();
    }

    /**
     * Invokes the login user operation to log an existing user into the network on this node
     * @param user
     * @param plainTextPassword
     * @return
     * @throws IOException
     */
    public boolean loginUser(User user, String plainTextPassword) throws IOException {
        LoginUserOperation operation = new LoginUserOperation(server, localNode, config, user, plainTextPassword);
        operation.execute();

        if (operation.isLoggedIn()) {
            localUser = user; // set the local user object for future reference
        }
        return operation.isLoggedIn();
    }

    /**
     * Stores a user on the local node
     * @param newUser The user to store on the local node
     * @return False if the user already exists on the network
     */
    public boolean addUser(User newUser) {
        for (User user: users) {
            if (user.getUserName().equals(newUser.getUserName())) {
                logger.debug("User with the same name exists - discarding");
                return false;
            }
        }
        users.add(newUser);
        logger.info("New user added to users: {}", newUser.getUserName());
        return true;
    }

    /**
     * Looks for a user on the local node and returns the object if it was found
     * @param newUser
     * @return The user object that was found
     */
    public User matchUser(User newUser) {
        for (User user: users) {
            if (user.getUserId().equals(newUser.getUserId())) {
                logger.debug("Found a matching user");
                return user;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return users;
    }
}
