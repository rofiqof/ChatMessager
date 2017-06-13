package app.com.mychat;


public class User
{
    String uname;
    String uimage;
    private String email;
    private String uid;

    public String getUimage()
    {
        return uimage;
    }

    public void setUimage(String uimage)
    {
        this.uimage = uimage;
    }

    public String getUname()
    {
        return uname;
    }

    public void setUname(String uname)
    {
        this.uname = uname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
