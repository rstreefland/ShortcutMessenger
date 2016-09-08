package uk.co.streefland.rhys.finalyearproject.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.jvm.hotspot.asm.Register;
import uk.co.streefland.rhys.finalyearproject.node.KeyId;
import uk.co.streefland.rhys.finalyearproject.node.Node;
import uk.co.streefland.rhys.finalyearproject.operation.RegisterUserOperation;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by Rhys on 07/09/2016.
 */
public class Users {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

    public boolean registerUser(User user) throws IOException {
        RegisterUserOperation operation = new RegisterUserOperation(server, localNode, config, user);
        operation.execute();
        return operation.isError();
    }

    public void loginUser() {

    }

    /**
     * Returns false if the user already exists
     * @param newUser
     * @return
     */
    public boolean addUser(User newUser) {
        for (User user: users) {
            if (user.getUserName().equals(newUser.getUserName())) {
                logger.info("User with the same name exists - discarding");
                return false;
            }
        }
        users.add(newUser);
        logger.info("New user added to users: {}", newUser.getUserName());
        return true;
    }

    public List<User> getUsers() {
        return users;
    }
}
