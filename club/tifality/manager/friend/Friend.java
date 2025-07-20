package club.tifality.manager.friend;

public final class Friend {
    private final String username;
    private String alias;

    public Friend(String username) {
        this(username, username);
    }

    public Friend(String alias, String username) {
        this.alias = alias;
        this.username = username;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getUsername() {
        return this.username;
    }
}

