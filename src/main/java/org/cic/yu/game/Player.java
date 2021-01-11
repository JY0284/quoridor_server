package org.cic.yu.game;

public class Player {
    private static int ID_COUNTER = 0;

    private int id;
    private String macAddress;

    public Player() {
        this.id = ID_COUNTER++;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return "Player:" + Integer.toString(id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Player) {
            return this.hashCode() == obj.hashCode();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                '}';
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }
}
