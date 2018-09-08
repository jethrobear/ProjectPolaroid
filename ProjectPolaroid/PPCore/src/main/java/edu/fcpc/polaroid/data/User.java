package edu.fcpc.polaroid.data;

public class User {
    int _usernumber;
    String _lastname;
    String _firstname;
    int _registermonth;
    int _registerday;
    int _registeryear;
    String _username;
    String _password;

    public User() {
    }

    public User(String lastname, String firstname, String username, String password) {
        _lastname = lastname;
        _firstname = firstname;
        _username = username;
        _password = password;
    }

    public User(String lastname, String firstname, int birthmonth, int birthday, int birthyear, String username, String password) {
        _lastname = lastname;
        _firstname = firstname;
        _registermonth = birthmonth;
        _registerday = birthday;
        _registeryear = birthyear;
        _username = username;
        _password = password;
    }

    public void setUsernumber(int usernumber) {
        _usernumber = usernumber;
    }

    public int getUsernumber() {
        return _usernumber;
    }

    public void setLastname(String lastname) {
        _lastname = lastname;
    }

    public String getLastname() {
        return _lastname;
    }

    public void setFirstname(String firstname) {
        _firstname = firstname;
    }

    public String getFirstname() {
        return _firstname;
    }

    public void setRegistermonth(int registermonth) {
        _registermonth = registermonth;
    }

    public int getRegistermonth() {
        return _registermonth;
    }

    public void setRegisterday(int registerday) {
        _registerday = registerday;
    }

    public int getRegisterday() {
        return _registerday;
    }

    public void setRegisteryear(int registeryear) {
        _registeryear = registeryear;
    }

    public int getRegisteryear() {
        return _registeryear;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public String getUsername() {
        return _username;
    }

    public void setPassword(String password) {
        _password = password;
    }

    public String getPassword() {
        return _password;
    }
}