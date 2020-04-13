import java.sql.*;
import java.util.*;

public class Editing {
    private Statement statement = null;
    private ResultSet rs = null;


    // CONSTRUCTOR
    public Editing(Connection connection)
    {
        try
        {
            statement = connection.createStatement();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------- //
    // OPERATION 1
    // for publications
    public void op1_insert_pub_book(String pubTitle, int pubId, int edition, float price, String ISBN, String pubDate, String[] topicList) throws SQLException{
        String query = "INSERT INTO Publications values("+pubId+",'"+pubTitle+"')";
        statement.executeUpdate(query);

        query = "INSERT INTO Books(pubId) values("+pubId+")";
        statement.executeUpdate(query);

        for(String topic: topicList) {
            String check_query = "SELECT * FROM Topics where topicName='"+topic+"'";
            rs = statement.executeQuery(check_query);
            if(!rs.next()){
                query = "INSERT INTO Topics values('"+topic+"')";
                statement.executeUpdate(query);
            }
            query = "INSERT INTO BookTopicMappings values(" + pubId + ",'" + topic + "')";
            statement.executeUpdate(query);
        }

        query = "INSERT INTO OrderItems values("+edition+","+pubId+","+price+",'"+pubDate+"')";
        statement.executeUpdate(query);

        query = "INSERT INTO Editions values("+edition+","+pubId+",'"+ISBN+"')";
        statement.executeUpdate(query);
    }

    public void op1_insert_pub_periodic(String pubTitle, int pubId, String periodicityType, String frequency, int issueNo, float price, String pubDate) throws SQLException{

        String query = "INSERT INTO Publications values ("+pubId+",'"+pubTitle+"')";
        statement.executeUpdate(query);

        query = "INSERT INTO PeriodicPublications values ("+pubId+",'"+periodicityType+"','"+frequency+"')";
        statement.executeUpdate(query);

        query = "INSERT INTO OrderItems values("+issueNo+","+pubId+","+price+",'"+pubDate+"')";
        statement.executeUpdate(query);

        query = "INSERT INTO Issues(orderItemId,pubId) values("+issueNo+","+pubId+")";
        statement.executeUpdate(query);
    }

    public void op1_insert_chapter(int pubId, int orderItemId){
        int ncmId;
        String title, chapterText, creationDate, topic, ids;

        Scanner intScanner = new Scanner(System.in);
        Scanner lineScanner = new Scanner(System.in);

        System.out.println("Enter the Title of chapter: ");
        title = lineScanner.nextLine();
        System.out.println("Enter the Chapter Text: ");
        chapterText = lineScanner.nextLine();
        System.out.println("Enter the Creation date of chapter: ");
        creationDate = lineScanner.nextLine();

        System.out.println("Enter the topics for this chapter (separated by commas only): ");
        topic = lineScanner.nextLine();
        String[] topics = topic.split(",");

        System.out.println("Enter the IDs of authors of this chapter (separated by commas only): ");
        ids = lineScanner.nextLine();

        String[] idArray = ids.split(",");
        int[] cmId = new int[idArray.length];

        for(int i=0;i<idArray.length;i++){
            cmId[i]=Integer.parseInt(idArray[i]);
        }

        try {
            op5_insert_chapter(title, orderItemId, pubId, chapterText, creationDate, topics, cmId);
            System.out.println("Chapter successfully inserted!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void op1_insert_article(int pubId, int orderItemId){
        int ncmId;
        String title, articleText, creationDate, topic, ids;

        Scanner intScanner = new Scanner(System.in);
        Scanner lineScanner = new Scanner(System.in);


        System.out.println("Enter the Title of article: ");
        title = lineScanner.nextLine();
        System.out.println("Enter the Article Text: ");
        articleText = lineScanner.nextLine();
        System.out.println("Enter the Creation date of article: ");
        creationDate = lineScanner.nextLine();

        System.out.println("Enter the of topics for this article seperated by a comma: ");
        topic = lineScanner.nextLine();
        String[] topics = topic.split(",");

        System.out.println("Enter the IDs of journalists of this chapter separated by commas: ");
        ids = lineScanner.nextLine();
        String[] idArray = ids.split(",");
        int[] cmId = new int[idArray.length];

        for(int i=0;i<idArray.length;i++){
            cmId[i]=Integer.parseInt(idArray[i]);
        }


        try {
            op5_insert_article(title, orderItemId, pubId, articleText, creationDate, topics, cmId);
            System.out.println("Article successfully inserted!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ----------------------------------------------------------------- //
    // OPERATION 2
    public void op2_update_pub(String pubTitle, int pubId) throws SQLException{
        String query = "UPDATE Publications SET title='"+pubTitle+"' WHERE pubId="+pubId;
        statement.executeUpdate(query);
    }
    public void op2_update_books(int pubId, int noOfEditions) throws SQLException{
        String query = "UPDATE Books SET noOfEditions="+noOfEditions+" WHERE pubId="+pubId;
        statement.executeUpdate(query);
    }
    public void op2_update_periodicty(int pubId, String periodicityType) throws SQLException{
        String query = "UPDATE PeriodicPublications SET periodicityType='"+periodicityType+"' WHERE pubId="+pubId;
        statement.executeUpdate(query);
    }
    public void op2_update_frequency(int pubId, String frequency) throws SQLException{
        String query = "UPDATE PeriodicPublications SET frequency='"+frequency+"' WHERE pubId="+pubId;
        statement.executeUpdate(query);
    }
    
    // ----------------------------------------------------------------- //
    // OPERATION 3
    public void op3_assign_editor_pub(int[] cmId, int orderItemId, int pubId) throws SQLException{
        for (int cId : cmId) {
            String query = "INSERT INTO ItemEditedBy(cmId,orderItemId,pubId) values(" + cId + "," + orderItemId + "," + pubId + ")";
            statement.executeUpdate(query);
        }
    }

    // ----------------------------------------------------------------- //
    // OPERATION 4
    public void op4_find_editor_pub(int cmId) throws SQLException{
        String query = "SELECT p.pubId, p.title, o.orderItemId, o.pubDate FROM ItemEditedBy ie NATURAL JOIN Publications p NATURAL JOIN OrderItems o WHERE ie.cmId="+cmId;
        rs = statement.executeQuery(query);

        /*
        int count=1;
        while(rs.next()){
            System.out.println("\nRecord "+count+": ");
            System.out.println("Publication ID: "+rs.getString("pubId"));
            System.out.println("Publication Title: "+rs.getString("title"));
            count++;
        }
        */

        TableGenerator tableGenerator = new TableGenerator();
        List<String> headersList = new ArrayList<>(); 
        headersList.add("Publication ID");
        headersList.add("Publication Title");
        headersList.add("Edition or Issue No.");
        headersList.add("Publication Date");

        List<List<String>> rowsList = new ArrayList<>();

        while(rs.next())
        {
            List<String> row = new ArrayList<>(); 

            row.add(rs.getString("pubId"));
            row.add(rs.getString("title"));
            row.add(rs.getString("orderItemId"));
            row.add(rs.getString("pubDate"));

            rowsList.add(row);
        }

        System.out.println(tableGenerator.generateTable(headersList, rowsList));
    }

    // ----------------------------------------------------------------- //
    // OPERATION 5
    public void op5_insert_chapter(String title, int orderItemId, int pubId, String chapterText, String creationDate, String[] topicName, int[] cmId) throws SQLException{
        String query = "INSERT INTO Chapters values ('"+title+"',"+orderItemId+","+pubId+",'"+chapterText+"','"+creationDate+"')";
        statement.executeUpdate(query);

        for (String topic:topicName) {
            String check_query = "SELECT * FROM Topics where topicName='"+topic+"'";
            rs = statement.executeQuery(check_query);
            if(!rs.next()){
                query = "INSERT INTO Topics values('"+topic+"')";
                statement.executeUpdate(query);
            }
            query = "INSERT INTO ChapterTopicMappings values ('"+title+"',"+orderItemId+","+pubId+",'"+topic+"')";
            statement.executeUpdate(query);
        }

        for (int cId : cmId){
            query = "INSERT INTO ChapterWrittenBy values ('"+title+"',"+orderItemId+","+pubId+","+cId+")";
            statement.executeUpdate(query);
        }
    }

    public void op5_insert_article(String title, int orderItemId, int pubId, String articleText, String creationDate, String[] topicName, int[] cmId) throws SQLException{
        String query = "INSERT INTO Articles(title, orderItemId, pubId, articleText, creationDate) values ('"+title+"',"+orderItemId+","+pubId+",'"+articleText+"','"+creationDate+"')";
        statement.executeUpdate(query);

        for (String topic:topicName) {
            String check_query = "SELECT * FROM Topics where topicName='"+topic+"'";
            rs = statement.executeQuery(check_query);
            if(!rs.next()){
                query = "INSERT INTO Topics values('"+topic+"')";
                statement.executeUpdate(query);
            }
            query = "INSERT INTO ArticleTopicMappings values ('"+title+"',"+orderItemId+","+pubId+",'"+topic+"')";
            statement.executeUpdate(query);
        }

        for (int cId : cmId){
            query = "INSERT INTO ArticleWrittenBy values ('"+title+"',"+orderItemId+","+pubId+","+cId+")";
            statement.executeUpdate(query);
        }
    }

    public void op5_delete_chapter(String title, int orderItemId, int pubId) throws SQLException{
        String query = "DELETE FROM Chapters WHERE title='"+title+"' AND orderItemId="+orderItemId+" AND pubId="+pubId;
        statement.executeUpdate(query);
    }

    public void op5_delete_article(String title, int orderItemId, int pubId) throws SQLException{
        String query = "DELETE FROM Articles WHERE title='"+title+"' AND orderItemId="+orderItemId+" AND pubId="+pubId;
        statement.executeUpdate(query);
    }

    // ----------------------------------------------------------------- //
    // ----------------------------------------------------------------- //


}