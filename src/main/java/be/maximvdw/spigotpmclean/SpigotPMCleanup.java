package be.maximvdw.spigotpmclean;

import be.maximvdw.spigotpmclean.config.Configuration;
import be.maximvdw.spigotpmclean.ui.Console;
import be.maximvdw.spigotsite.SpigotSiteCore;
import be.maximvdw.spigotsite.api.SpigotSite;
import be.maximvdw.spigotsite.api.exceptions.ConnectionFailedException;
import be.maximvdw.spigotsite.api.user.Conversation;
import be.maximvdw.spigotsite.api.user.ConversationManager;
import be.maximvdw.spigotsite.api.user.User;
import be.maximvdw.spigotsite.api.user.exceptions.InvalidCredentialsException;
import be.maximvdw.spigotsite.api.user.exceptions.TwoFactorAuthenticationException;

import java.util.List;

/**
 * SpigotPMCleanup
 * Created by Maxim on 6/06/2019.
 */
public class SpigotPMCleanup {
    /* Authenticated user */
    private User user = null;

    public SpigotPMCleanup(String... args) throws ConnectionFailedException {
        Console.info("Initializing Spigot PM Cleanup v1.0.0 ...");
        new SpigotSiteCore();
        new Configuration(1); // Version 1

        String username = Configuration.getString("username");
        String password = Configuration.getString("password");
        String totpSecret = Configuration.getString("2fakey");

        Console.info("Logging in " + username + " ...");
        try {
            User user = SpigotSite.getAPI().getUserManager()
                    .authenticate(username, password, totpSecret);
            setUser(user);
        } catch (InvalidCredentialsException e) {
            Console.info("Unable to log in! Wrong credentials!");
            return;
        } catch (TwoFactorAuthenticationException e) {
            Console.info("Unable to log in! Two factor authentication failed!");
            return;
        } catch (ConnectionFailedException e) {
            Console.info("Unable to log in! Connection failed!");
            return;
        }
        password = null;
        totpSecret = null;

        Console.info("Fetching 10000 conversations (read and unread), this can take a while ...");
        ConversationManager conversationManager = SpigotSite.getAPI()
                .getConversationManager();
        List<Conversation> conversations = conversationManager
                .getConversations(user, 10000);
        for (Conversation conv : conversations) {
            if (conv.isUnread()) {
                Console.info("Marking conversation as read: [#" + conv.getConverationId() + "]" + conv.getTitle() + " by " + conv.getAuthor().getUsername());
                if (!Configuration.getBoolean("debug")) {
                    conv.markAsRead(user);
                }
            }
        }
    }

    public static void main(String... args) throws ConnectionFailedException {
        new SpigotPMCleanup(args);
    }

    public User getUser() {
        return user;
    }

    private void setUser(User user) {
        this.user = user;
    }
}
