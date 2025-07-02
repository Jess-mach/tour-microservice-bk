package br.com.tourapp.dto;

public class GoogleUserInfo {
    private final String sub;
    private final String email;
    private final String name;
    private final String picture;

    public GoogleUserInfo(String sub, String email, String name, String picture) {
        this.sub = sub;
        this.email = email;
        this.name = name;
        this.picture = picture;
    }

    // Getters
    public String getSub() { return sub; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPicture() { return picture; }

    @Override
    public String toString() {
        return "GoogleUserInfo{" +
                "sub='" + sub + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", picture='" + picture + '\'' +
                '}';
    }
}