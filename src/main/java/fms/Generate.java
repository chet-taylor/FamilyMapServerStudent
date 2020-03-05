package fms;


import fms.Dao.EventDao;
import fms.Dao.PersonDao;
import fms.Exceptions.DataAccessException;
import fms.Model.Event;
import fms.Model.Person;
import fms.Model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class Generate {

    /**
     * Default constructor
     */
    public Generate(){}



    public void generateInfo(User user, int generation, Connection conn) throws DataAccessException, IOException {
        PersonDao pDao = new PersonDao(conn);
        EventDao eDao = new EventDao(conn);
        Person userPerson = pDao.getPerson(user.getPersonID());
        //Add at least the original user
        pDao.addPerson(userPerson);
        //Original user has to have at least a birth
        final int CURRENT_YEAR = 2020;
        int year = CURRENT_YEAR - 25;
        eDao.addEvent(generateEvent(user.getUserName(),userPerson.getPersonID(),getRandomLocation(),
                "birth",year));
        //Add additional generations as requested recursively
        recurse(userPerson, generation, pDao, eDao);
    }

    private void recurse(Person child, int generation, PersonDao pDao, EventDao eDao) throws DataAccessException, IOException {
        //base case
        if(generation == 0) { return; }
        //Father Data
        String fathersFatherID = getParentID(generation-1);
        String fathersMotherID = getParentID(generation-1);
        String mFirstName = getMaleName();
        String fatherPersonID = child.getFatherID();
        //Mother Data
        String mothersFatherID = getParentID(generation-1);
        String mothersMotherID = getParentID(generation-1);
        String fFirstName = getFemaleName();
        String motherPersonID = child.getMotherID();
        //Gender neutral data
        String lastName = getSurname();
        String associatedUsername = child.getUsername();
        int year = eDao.getEvent(child.getPersonID()+"-"+"birth").getYear();
        Location marriageLocation = getRandomLocation();

        //Generate and link father of person
        Person father = new Person(fatherPersonID, associatedUsername, mFirstName, lastName, "m",
                fathersFatherID, fathersMotherID, motherPersonID);
        pDao.addPerson(father);
        //Generate birth, marriage, death
        eDao.addEvent(generateEvent(child.getUsername(), father.getPersonID(), getRandomLocation(),
                "birth",year-25));
        eDao.addEvent(generateEvent(child.getUsername(), father.getPersonID(), marriageLocation,
                "marriage",year-5));
        eDao.addEvent(generateEvent(child.getUsername(), father.getPersonID(), getRandomLocation(),
                "death",year+30));
        recurse(father, generation-1, pDao, eDao);


        //Generate and link mother of person
        Person mother = new Person(motherPersonID, associatedUsername, fFirstName, lastName, "f",
                mothersFatherID, mothersMotherID, fatherPersonID);
        pDao.addPerson(mother);
        //Generate birth, marriage, death
        eDao.addEvent(generateEvent(child.getUsername(), mother.getPersonID(), getRandomLocation(),
                "birth",year-25));
        eDao.addEvent(generateEvent(child.getUsername(), mother.getPersonID(), marriageLocation,
                "marriage",year-5));
        eDao.addEvent(generateEvent(child.getUsername(), mother.getPersonID(), getRandomLocation(),
                "death",year+30));
        recurse(mother, generation-1, pDao, eDao);
    }


    private Event generateEvent(String associatedUsername, String personID, Location location, String eventType, int year) throws IOException {
        JsonStrings jStrings = new JsonStrings();
        String locations = jStrings.getLocations();
        LocationData jsonLocations = JsonSerializer.deserialize(locations, LocationData.class);
        ArrayList<Location> locationList = jsonLocations.getData();
        String eventID = personID+"-"+eventType;

        Event result = new Event(eventID, associatedUsername, personID,location.getLatitude(),
                location.getLongitude(),location.getCountry(),location.getCity(),eventType,year);

        return result;
    }

    private String getParentID(int generations){
        if(generations == 0){
            return null;
        }
        else{ //get random
            return UUID.randomUUID().toString();
        }
    }

    private String getMaleName() throws IOException {
        JsonStrings jStrings = new JsonStrings();
        String mnames = jStrings.getMnames();
        NameData maleNames = JsonSerializer.deserialize(mnames, NameData.class);
        Random rand = new Random();
        ArrayList<String> finalNames = maleNames.getData();
        return finalNames.get(rand.nextInt(finalNames.size()));
    }

    private String getFemaleName() throws IOException {
        JsonStrings jStrings = new JsonStrings();
        String fnames = jStrings.getFnames();
        NameData femaleNames = JsonSerializer.deserialize(fnames, NameData.class);
        Random rand = new Random();
        ArrayList<String> finalNames = femaleNames.getData();
        return finalNames.get(rand.nextInt(finalNames.size()));
    }

    private String getSurname() throws IOException {
        JsonStrings jStrings = new JsonStrings();
        String snames = jStrings.getSnames();
        NameData surnames = JsonSerializer.deserialize(snames, NameData.class);
        Random rand = new Random();
        ArrayList<String> finalNames = surnames.getData();
        return finalNames.get(rand.nextInt(finalNames.size()));
    }

    private Location getRandomLocation() throws IOException {
        JsonStrings jStrings = new JsonStrings();
        String jsonLocations = jStrings.getLocations();
        LocationData ld = JsonSerializer.deserialize(jsonLocations, LocationData.class);
        ArrayList<Location> locations = ld.getData();
        Random rand = new Random();
        return locations.get(rand.nextInt(locations.size()));
    }



    //******************************************************************************************************************

    /**
     * Definition of location, used to generate Event objects in outer class
     */
    private class Location{
        /**
         * The country of the location
         */
        private String country;
        /**
         * The city of the location
         */
        private String city;
        /**
         * The longitude of the location
         */
        private Float longitude;
        /**
         * The latitude of the location
         */
        private Float latitude;

        /**
         * Default constructor
         */
        public Location(){}

        public String getCountry() {
            return country;
        }

        public String getCity() {
            return city;
        }

        public Float getLongitude() {
            return longitude;
        }

        public Float getLatitude() {
            return latitude;
        }
    }

    //******************************************************************************************************************

    /**
     * The middle man between the given JSON and giving the location info to the outer class
     */
    private class LocationData{
        /**
         * The locations from the JSON
         */
        private ArrayList<Location> data;

        /**
         * Default constructor
         */
        LocationData(){}

        /**
         * get the JSON data
         * @return the JSON data about locations
         */
        ArrayList<Location> getData() { return data; }
    }

    //******************************************************************************************************************

    /**
     * The middle man between the given JSON and giving it as usable name data to the outer class
     */
    private class NameData{
        /**
         * The names from the JSON
         */
        private ArrayList<String> data;

        /**
         * Default constructor
         */
        NameData(){}

        /**
         * get the JSON data
         * @return the JSON data about locations
         */
        ArrayList<String> getData() { return data; }
    }

    //******************************************************************************************************************

    private class JsonStrings{
        /**
         * provided JSON female names
         */
        String fnames;
        /**
         * provided JSON male names
         */
        String mnames;
        /**
         * provided JSON surnames
         */
        String snames;
        /**
         * provided JSON locations
         */
        String locations;

        /**
         * Default constructor
         */
        JsonStrings() throws IOException {
            fnames = new String(Files.readAllBytes(Paths.get("json/fnames.json")));
            mnames = new String(Files.readAllBytes(Paths.get("json/mnames.json")));
            snames = new String(Files.readAllBytes(Paths.get("json/snames.json")));
            locations = new String(Files.readAllBytes(Paths.get("json/locations.json")));
        }

        public String getFnames() {
            return fnames;
        }

        public String getMnames() {
            return mnames;
        }

        public String getSnames() {
            return snames;
        }

        public String getLocations() {
            return locations;
        }
    }


    //******************************************************************************************************************

}