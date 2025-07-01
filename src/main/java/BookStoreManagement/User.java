
package BookStoreManagement;


public class User {
    private String username;
    private String password;
    private String key;
    private String keyIssueDate;
    private String expiryDate;
    private String Role;

    public User(String username, String password, String key, String keyIssueDate, String expiryDate,String Role) {
        this.username = username;
        this.password = password;
        this.key = key;
        this.keyIssueDate = keyIssueDate;
        this.expiryDate = expiryDate;
        this.Role = Role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getKey() { return key; }
    public String getKeyIssueDate() { return keyIssueDate; }
    public String getExpiryDate() { return expiryDate; }
    public String getRole(){return Role;}
}
