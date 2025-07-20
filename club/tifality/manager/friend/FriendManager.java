package club.tifality.manager.friend;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import club.tifality.utils.FileUtils;
import club.tifality.utils.Wrapper;
import club.tifality.utils.handler.Manager;

public final class FriendManager extends Manager<Friend> {
    public static final File FRIENDS_FILE = new File("LitelyWare", "friends.txt");

    public FriendManager() {
        super(FriendManager.loadFriends());
        if (!FRIENDS_FILE.exists()) {
            try {
                boolean bl = FRIENDS_FILE.createNewFile();
            } catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    private static List<Friend> loadFriends() {
        ArrayList<Friend> friends = new ArrayList<>();
        if (FRIENDS_FILE.exists()) {
            List<String> lines = FileUtils.getLines(FRIENDS_FILE);
            for (String line : lines) {
                String[] split;
                if (!line.contains(":") || line.length() <= 2 || (split = line.split(":")).length != 2) continue;
                friends.add(new Friend(split[0], split[1]));
            }
        }
        return friends;
    }

    public boolean isFriend(String username) {
        for (Friend friend : this.getElements()) {
            if (!friend.getUsername().equalsIgnoreCase(username)) continue;
            return true;
        }
        return false;
    }

    public String friend(String friendName, String alias) {
        for (EntityPlayer player : Wrapper.getLoadedPlayers()) {
            String username = player.getGameProfile().getName();
            if (!username.equalsIgnoreCase(friendName)) continue;
            this.getElements().add(new Friend(alias, friendName));
            return username;
        }
        return null;
    }

    public String unfriend(String username) {
        for (Friend friend : this.getElements()) {
            String friendName = friend.getUsername();
            if (!friendName.equalsIgnoreCase(username)) continue;
            this.getElements().remove(friend);
            return friendName;
        }
        return null;
    }

    public boolean isFriend(EntityPlayer player) {
        return this.isFriend(player.getGameProfile().getName());
    }
}

