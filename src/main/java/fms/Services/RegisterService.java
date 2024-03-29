package fms.Services;

import fms.Dao.AuthTokenDao;
import fms.Dao.PersonDao;
import fms.Dao.UserDao;
import fms.Database;
import fms.Exceptions.DataAccessException;
import fms.Generate;
import fms.Model.AuthToken;
import fms.Model.Person;
import fms.Model.User;
import fms.Requests.RegisterRequest;
import fms.Responses.RegisterResponse;
import java.io.IOException;
import java.sql.Connection;
import java.util.UUID;

/**
 * Login Register JSON Request and also completes the Register request
 * by using the Dao package classes
 */
public class RegisterService {

    /**
     * default constructor
     */
    public RegisterService() {
    }

    /**
     * Takes care of registering new users in the database
     * @param request All the info about the request
     * @return RegisterResponse object with info about the request
     */
    public RegisterResponse register(RegisterRequest request) throws DataAccessException, IOException {
        Database db = new Database();
        Connection conn = db.openConnection();
        //Create a new account
        //request contains:
        //make user and store in database
        UserDao uDao = new UserDao(conn);
        String userName = request.getUserName();
        //if user already exists or is null, can't register them
        User testIfExits = uDao.getUser(userName);
        if(testIfExits != null || userName == null){
            db.closeConnection(true);
            return new RegisterResponse("Error: invalid username", false);
        }
        String personID = UUID.randomUUID().toString();
        User user = new User(userName, request.getPassword(), request.getEmail(), request.getFirstName(),request.getLastName(),
                request.getGender(), personID);
        uDao.addUser(user);
        //generate 4 generations of ancestor data
        PersonDao pDao = new PersonDao(conn);
        Person userPerson = new Person(user.getPersonID(), user.getUserName(), user.getFirstName(), user.getLastName(),
                user.getGender(), UUID.randomUUID().toString(), UUID.randomUUID().toString(),null);
        db.closeConnection(true);
        Generate getData = new Generate();
        getData.generateInfo(userPerson, 4);
        conn = db.openConnection();
        //logs the user in
        //Register attempt seems valid, try to get authToken
        AuthTokenDao atDao = new AuthTokenDao(conn);
        String authToken = atDao.generateAuthToken();
        atDao.addAuthToken(new AuthToken(authToken, userName));
        //all done, make sure to return authToken
        db.closeConnection(true);
        return new RegisterResponse(authToken, userName, personID,true);
    }
}
