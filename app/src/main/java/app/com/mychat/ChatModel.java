package app.com.mychat;

import android.graphics.Bitmap;

/**
 * Created by isquare3 on 12/17/16.
 */

class ChatModel
{
    //private variables
    int _id;
    String _name;
    String _phone_number;
    String _file;
    String _messaeg;
    String _type;
    String _time;
    String _fromId;
    String _toId;
    String _msgId;
    String _video;
    String _thumb;
    String vdo;
    String _audio;
    String _read;

    public ChatModel()
    {

    }

    public ChatModel(String message_id, String time, String type, String fromid,
                     String toid, String message, String image ,String video , String thumb,String vdo,String audio,String read)
    {
        this._msgId=message_id;
        this._time= time;
        this._type=type;
        this._fromId=fromid;
        this._toId=toid;
        this._messaeg=message;
        this._file=image;
        this._video=video;
        this._thumb=thumb;
        this.vdo=vdo;
        this._audio=audio;
        this._read=read;
    }

    public String get_read() {
        return _read;
    }

    public void set_read(String _read) {
        this._read = _read;
    }

    public String get_audio() {
        return _audio;
    }

    public void set_audio(String _audio) {
        this._audio = _audio;
    }

    public String getVdo() {
        return vdo;
    }

    public void setVdo(String vdo)
    {
        this.vdo = vdo;
    }

    public String get_thumb() {
        return _thumb;
    }

    public void set_thumb(String _thumb) {
        this._thumb = _thumb;
    }

    public String get_video() {
        return _video;
    }

    public void set_video(String _video) {
        this._video = _video;
    }

    public int get_id()
    {
        return _id;
    }

    public void set_id(int _id)
    {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_phone_number() {
        return _phone_number;
    }

    public void set_phone_number(String _phone_number) {
        this._phone_number = _phone_number;
    }

    public String get_file() {
        return _file;
    }

    public void set_file(String _file) {
        this._file = _file;
    }

    public String get_messaeg() {
        return _messaeg;
    }

    public void set_messaeg(String _messaeg) {
        this._messaeg = _messaeg;
    }

    public String get_type() {
        return _type;
    }

    public void set_type(String _type) {
        this._type = _type;
    }

    public String get_time() {
        return _time;
    }

    public void set_time(String _time) {
        this._time = _time;
    }

    public String get_fromId() {
        return _fromId;
    }

    public void set_fromId(String _fromId) {
        this._fromId = _fromId;
    }

    public String get_toId() {
        return _toId;
    }

    public void set_toId(String _toId) {
        this._toId = _toId;
    }

    public String get_msgId() {
        return _msgId;
    }

    public void set_msgId(String _msgId) {
        this._msgId = _msgId;
    }
}
