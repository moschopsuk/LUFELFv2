package uk.ac.lancs.LUFELFv2.commsV2;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Luke on 06/03/14.
 */
public class ServerFactory {
    public static final String SERVERIP = "148.88.32.47";
    private static final String TAG = "ServerAPI";
    private static ServerFactory instance = null;
    private Boolean isLoggedIn;
    private User userLoggedIn;
    private String hashPassword;

    /**
     * Singleton Instance
     */
    protected ServerFactory() {
        this.isLoggedIn = false;
    }

    /**
     * Creates a static singleton class
     * this means we can only have one instace
     * in the whole application
     * @return
     */
    public static ServerFactory getInstance() {
        if(instance == null) {
            instance = new ServerFactory();
        }
        return instance;
    }

    /**
     * Returns if the user is logged in
     * @return
     */
    public Boolean isLoggedIn() {
        return this.isLoggedIn;
    }

    /**
     * Converts a map into a url parameter
     * @param map
     * @return
     */
    private String buildURL(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<?,?> entry : map.entrySet()) {

            if (sb.length() > 0) {
                sb.append("&");
            }

            sb.append(String.format("%s=%s", entry.getKey().toString(), entry.getValue().toString()));
        }

        return sb.toString();
    }

    /**
     * Handles the post to the server
     * @param file
     * @param args
     * @return
     */
    private String doConnection(String file, String args) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL website = new URL("http://" + SERVERIP +"/" + file + ".php?" + args);
            Log.d(TAG, "URL" + website.toString());
            connection = (HttpURLConnection)website.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        } catch(MalformedURLException e) {
            Log.e(TAG, e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, e.getMessage());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            response.append(inputLine);

        in.close();
        return response.toString();
    }

    /**
     * Conver xml string into xml document
     * @param xml
     * @return
     * @throws IOException
     */
    private Document phaseXML(String xml) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xml));
            return builder.parse(is);

        } catch(ParserConfigurationException e) {
            Log.e(TAG, e.getMessage());
        } catch(SAXException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

    /**
     * Conver string to md5 hash
     * @param message
     * @return
     */
    private String MD5(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2*hash.length);
            for(byte b : hash){
                sb.append(String.format("%02x", b&0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return digest;
    }

    /**
     * Check for a a valid response
     * @param xml
     * @return
     * @throws APIException
     */
    private boolean getResponse(Document xml) throws APIException {
        String status;
        String message;
        int code;

        xml.getDocumentElement().normalize();
        Node rsp = xml.getElementsByTagName("rsp").item(0);
        status = rsp.getAttributes().getNamedItem("status").getNodeValue();

        if (status.equals("ok"))
            return true;

        code = Integer.parseInt(rsp.getAttributes().getNamedItem("code").getNodeValue());
        Node messageNode = xml.getElementsByTagName("message").item(0);
        message = messageNode.getFirstChild().getNodeValue();

        throw new APIException(code, message);
    }

    /**
     * Perfroms a login reqest to the server
     * @param username
     * @param password
     * @throws APIException
     */
    public void login(String username, String password) throws APIException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("username", username);
        args.put("password", MD5(password));
        hashPassword =  MD5(password);

        try {
            String xml = doConnection("login_user", buildURL(args));
            Document xmlBody = phaseXML(xml);

            if(getResponse(xmlBody)) {
                this.isLoggedIn = true;
            }

            Node messageNode = xmlBody.getElementsByTagName("user_id").item(0);
            int id = Integer.parseInt(messageNode.getFirstChild().getNodeValue());
            userLoggedIn = getUser(id);

        } catch (IOException e) {
            throw new APIException(400, "Error Reading data");
        }
    }

    /**
     * Retuns to the user logged in
     * @return
     */
    public User getUser() {
        return userLoggedIn;
    }

    /**
     * Retuns a user based on ID
     * @param id
     * @return
     * @throws APIException
     */
    public User getUser(Integer id) throws APIException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id", id.toString());
        args.put("password", hashPassword);

        try {
            String xml = doConnection("query_user_details", buildURL(args));
            Document xmlBody = phaseXML(xml);
            User user = new User(id);

            if(getResponse(xmlBody)) {
                Node rsp = xmlBody.getElementsByTagName("rsp").item(0);
                Node userNode = rsp.getOwnerDocument().getElementsByTagName("user").item(0);

                user.setName(userNode.getOwnerDocument().getElementsByTagName("name").item(0).getTextContent());
                user.setLibaryCard(userNode.getOwnerDocument().getElementsByTagName("lib_no").item(0).getTextContent());
                user.setUsername(userNode.getOwnerDocument().getElementsByTagName("username").item(0).getTextContent());
                user.setDob(userNode.getOwnerDocument().getElementsByTagName("dob").item(0).getTextContent());
                user.setType(userNode.getOwnerDocument().getElementsByTagName("type").item(0).getTextContent());
                user.setDescription(userNode.getOwnerDocument().getElementsByTagName("description").item(0).getTextContent());

                Log.d(TAG, user.toString());

                return user;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Finds user based on username
     * @param username
     * @return
     * @throws APIException
     */
    public User getUser(String username) throws APIException {
        Map<String, String> args = new HashMap<String, String>();
        args.put("username", username);
        args.put("password", hashPassword);

        try {
            String xml = doConnection("query_user_details", buildURL(args));
            Document xmlBody = phaseXML(xml);

            if(getResponse(xmlBody)) {
                Node rsp = xmlBody.getElementsByTagName("rsp").item(0);
                Node userNode = rsp.getOwnerDocument().getElementsByTagName("user").item(0);

                int id = Integer.parseInt(userNode.getOwnerDocument().getElementsByTagName("user_id").item(0).getTextContent());

                User user = new User(id);

                user.setName(userNode.getOwnerDocument().getElementsByTagName("name").item(0).getTextContent());
                user.setLibaryCard(userNode.getOwnerDocument().getElementsByTagName("lib_no").item(0).getTextContent());
                user.setUsername(userNode.getOwnerDocument().getElementsByTagName("username").item(0).getTextContent());
                user.setDob(userNode.getOwnerDocument().getElementsByTagName("dob").item(0).getTextContent());
                user.setType(userNode.getOwnerDocument().getElementsByTagName("type").item(0).getTextContent());
                user.setDescription(userNode.getOwnerDocument().getElementsByTagName("description").item(0).getTextContent());

                Log.d(TAG, user.toString());

                return user;
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        return null;
    }

    /**
     * Adds an user to the database
     * @param username
     * @param password
     * @param libNo
     * @param name
     * @param dob
     * @param type
     * @param description
     * @param locStatus
     * @param accessLevel
     */
    public boolean register(String username, String password, String libNo, String name, String dob, String type, String description, String locStatus, String accessLevel) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("name",name);
        args.put("username",username);
        args.put("lib_no",libNo);
        args.put("dob",dob);
        args.put("password",password);
        args.put("type",type);
        args.put("description",description);
        args.put("location_status",locStatus);
        args.put("access_level",accessLevel);

        try {
            String xml = doConnection("create_user", buildURL(args));
            Document xmlBody = phaseXML(xml);
            Log.d(TAG, xml);

            return getResponse(xmlBody);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Gets list of events
     * @return
     */
    public ArrayList<EventItem> getEvents() {
        ArrayList<EventItem> events = new ArrayList<EventItem>();

        try {
            String xml = doConnection("query_event_list", "");
            Log.d(TAG, xml);
            Document xmlBody = phaseXML(xml);
            NodeList nodes = xmlBody.getElementsByTagName("event");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    EventItem event = new EventItem();
                    Element element = (Element) node;

                    event.setId(Integer.parseInt(element.getElementsByTagName("event_id").item(0).getTextContent()));
                    event.setName(element.getElementsByTagName("event_name").item(0).getTextContent());
                    event.setCreated(element.getElementsByTagName("event_date").item(0).getTextContent());

                    events.add(event);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return events;
    }

    /**
     * Get events details from event id
     * @param id
     * @return
     */
    public Event getEvent(String id) {
        Event event = new Event();
        Map<String, String> args = new HashMap<String, String>();
        args.put("event_id", id);
        args.put("password", hashPassword);

        try {
            String xml = doConnection("query_event_details", buildURL(args));
            Document xmlBody = phaseXML(xml);

            if (getResponse(xmlBody)) {
                Node rsp = xmlBody.getElementsByTagName("rsp").item(0);
                Node eventNode = rsp.getOwnerDocument().getElementsByTagName("event").item(0);

                event.setName(eventNode.getOwnerDocument().getElementsByTagName("name").item(0).getTextContent());
                event.setDate(eventNode.getOwnerDocument().getElementsByTagName("date").item(0).getTextContent());
                event.setType(eventNode.getOwnerDocument().getElementsByTagName("type").item(0).getTextContent());
                event.setDescription(eventNode.getOwnerDocument().getElementsByTagName("description").item(0).getTextContent());
                event.setLocationName(eventNode.getOwnerDocument().getElementsByTagName("location_name").item(0).getTextContent());
                event.setLocationAddress(eventNode.getOwnerDocument().getElementsByTagName("location_address").item(0).getTextContent());
                event.setLocation(eventNode.getOwnerDocument().getElementsByTagName("location").item(0).getTextContent());
                event.setUsername(eventNode.getOwnerDocument().getElementsByTagName("username").item(0).getTextContent());
                event.setEmail(eventNode.getOwnerDocument().getElementsByTagName("email").item(0).getTextContent());

                Node usersNode = xmlBody.getElementsByTagName("users").item(0);
                NodeList nodes = usersNode.getChildNodes();

                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        event.addAttendee(element.getFirstChild().getTextContent());
                    }
                }

                Log.d(TAG, event.toString());

            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (APIException e) {
            Log.e(TAG, e.toString());
        }

        return event;
    }

    /**
     * Marks the user as attending event
     * @param eventId
     * @return
     */
    public Boolean attendEvent(String eventId) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id", userLoggedIn.getId());
        args.put("event_id", eventId);
        args.put("password", hashPassword);

        try {
            String xml = doConnection("attend_event", buildURL(args));
            Document xmlBody = phaseXML(xml);

            return getResponse(xmlBody);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (APIException e) {
            Log.e(TAG, e.toString());
        }

        return false;
    }

    /**
     * Gets list of places on the db
     * @return
     */
    public ArrayList<Place> getPlaces(){
        ArrayList<Place> places = new ArrayList<Place>();

        try {
            String xml = doConnection("query_place_list", "");
            Log.d(TAG, xml);
            Document xmlBody = phaseXML(xml);
            NodeList nodes = xmlBody.getElementsByTagName("place");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Place place = new Place();
                    Element element = (Element) node;

                    place.setName(element.getElementsByTagName("place_name").item(0).getTextContent());
                    place.setLat(element.getElementsByTagName("place_lat").item(0).getTextContent());
                    place.setLon(element.getElementsByTagName("place_lon").item(0).getTextContent());

                    places.add(place);
                }
            }

            Log.d(TAG, "Places Fetched");

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        return places;
    }

    /**
     * add a friend with the userID
     * @param userId
     * @return
     */
    public boolean addFriend(String userId) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id1", userLoggedIn.getId());
        args.put("user_id2", userId);
        args.put("password", hashPassword);

        try {
            String xml = doConnection("make_friends", buildURL(args));
            Log.d(TAG, xml);
            Document xmlBody = phaseXML(xml);

            return getResponse(xmlBody);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (APIException e) {
            Log.e(TAG, e.toString());
        }

        return false;
    }

    /**
     * Returns a list of pending friends
     * @return
     */
    public ArrayList<PendingFriend> getPendingFriends() {
        ArrayList<PendingFriend> friends = new ArrayList<PendingFriend>();
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id", userLoggedIn.getId());
        args.put("password", hashPassword);

        try {
            String xml = doConnection("get_friend_requests", buildURL(args));
            Document xmlBody = phaseXML(xml);

            NodeList nodes = xmlBody.getElementsByTagName("user");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    PendingFriend friend = new PendingFriend();
                    Element element = (Element) node;

                    friend.setName(element.getElementsByTagName("name").item(0).getTextContent());
                    friend.setId(element.getElementsByTagName("user_id").item(0).getTextContent());
                    friend.setRequestId(element.getElementsByTagName("request_id").item(0).getTextContent());

                    friends.add(friend);
                }
            }

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }

        return friends;
    }

    /**
     * Allows a user to accept or deny a friend request
     * @param friend
     * @param accept
     * @return
     */
    public Boolean processRequest(PendingFriend friend, Boolean accept) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("request_id", friend.getRequestId());
        args.put("user_id1", userLoggedIn.getId());
        args.put("user_id2", friend.getId());
        args.put("password", hashPassword);
        args.put("status", ((accept) ? "1" : "2"));

        try {
            String xml = doConnection("friend_handshake", buildURL(args));
            Document xmlBody = phaseXML(xml);

            return getResponse(xmlBody);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (APIException e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }

    /**
     * Get list of friends
     * @return
     */
    public ArrayList<Friend> getFriends() {
        ArrayList<Friend> friends = new ArrayList<Friend>();
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id", userLoggedIn.getId());
        args.put("password", hashPassword);

        try {
            String xml = doConnection("query_friend_list", buildURL(args));
            Document xmlBody = phaseXML(xml);

            NodeList nodes = xmlBody.getElementsByTagName("user");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Friend friend = new Friend();
                    Element element = (Element) node;

                    friend.setName(element.getElementsByTagName("friend_name").item(0).getTextContent());
                    friend.setId(element.getElementsByTagName("friend_id").item(0).getTextContent());

                    friends.add(friend);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return friends;
    }

    /**
     * Send a message to another user
     * @param id
     * @param message
     * @return
     */
    public Boolean sendMessage(String id, String message) {
        Map<String, String> args = new HashMap<String, String>();
        String codedMessage = null;

        try {
            codedMessage = URLEncoder.encode(message, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            Log.e(TAG, e.toString());
        }

        args.put("user_id_from", userLoggedIn.getId());
        args.put("password", hashPassword);
        args.put("user_id_to", id);
        args.put("message", codedMessage);

        try {
            String xml = doConnection("send_message", buildURL(args));
            Document xmlBody = phaseXML(xml);

            return getResponse(xmlBody);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } catch (APIException e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }

    /**
     * Returns list of send messages
     * @return
     */
    public ArrayList<AppMessage> getSentMessages() {
        ArrayList<AppMessage> messages = new ArrayList<AppMessage>();
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id", userLoggedIn.getId());
        args.put("password", hashPassword);

        try {
            String xml = doConnection("query_sent_messages", buildURL(args));
            Document xmlBody = phaseXML(xml);

            Node rsp = xmlBody.getElementsByTagName("rsp").item(0);
            NodeList nodes = rsp.getFirstChild().getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    AppMessage msg = new AppMessage();
                    Element element = (Element) node;

                    msg.setId(element.getElementsByTagName("message_id").item(0).getTextContent());
                    msg.setRecipient(element.getElementsByTagName("message_to").item(0).getTextContent());
                    msg.setMessage(element.getElementsByTagName("message").item(0).getTextContent());

                    messages.add(msg);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return messages;
    }

    /**
     * Returns list of recived messages
     * @return
     */
    public ArrayList<AppMessage> getMessages() {
        ArrayList<AppMessage> messages = new ArrayList<AppMessage>();
        Map<String, String> args = new HashMap<String, String>();
        args.put("user_id", userLoggedIn.getId());
        args.put("password", hashPassword);

        try {
            String xml = doConnection("query_received_messages", buildURL(args));
            Document xmlBody = phaseXML(xml);

            Node rsp = xmlBody.getElementsByTagName("rsp").item(0);
            NodeList nodes = rsp.getFirstChild().getChildNodes();

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    AppMessage msg = new AppMessage();
                    Element element = (Element) node;

                    msg.setId(element.getElementsByTagName("message_id").item(0).getTextContent());
                    msg.setRecipient(element.getElementsByTagName("message_from").item(0).getTextContent());
                    msg.setMessage(element.getElementsByTagName("message").item(0).getTextContent());

                    messages.add(msg);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return messages;
    }

}

