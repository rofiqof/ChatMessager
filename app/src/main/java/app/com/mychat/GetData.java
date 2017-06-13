package app.com.mychat;

import android.graphics.Bitmap;

import java.io.File;
import java.util.Date;

/**
 * Created by isquare on 11/21/2015.
 */
//@ParseClassName("GetData")
public class GetData
{
    String body;
    String uid;
    boolean status;
    String imgLink;
    String type;
    Bitmap bitmap;
    String msgtime;
    String delever;
    String imgUrl;
    Date time;
    String message_Id;
    String read;

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getMessage_Id() {
        return message_Id;
    }

    public void setMessage_Id(String message_Id) {
        this.message_Id = message_Id;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImgLink() {
        return imgLink;
    }

    public void setImgLink(String imgLink) {
        this.imgLink = imgLink;
    }



    public String getDelever() {
        return delever;
    }

    public void setDelever(String delever) {
        this.delever = delever;
    }


    public Date getTime()
    {
        return time;
    }

    public void setTime(Date time)
    {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isStatus()
    {
        return status;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setStatus(boolean status)
    {
        this.status = status;
    }

    public String getBody()
    {
        return body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public String getMsgtime() {
        return msgtime;
    }

    public void setMsgtime(String msgtime) {
        this.msgtime = msgtime;
    }
}
