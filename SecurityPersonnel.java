import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The SecurityPersonnel class represents a security personnel managing citations and reports.
 */
public class SecurityPersonnel {
    private Connection conn;
    private Properties prop;
    private String userID;
    private Scanner scanner;

    /**
     * Constructs a SecurityPersonnel object with the specified user ID.
     *
     * @param userID The user ID of the security personnel.
     */
    public SecurityPersonnel(String userID) {
        this.userID = userID;
        this.scanner = new Scanner(System.in);

        try {
            FileInputStream input = new FileInputStream("config.properties");
            prop = new Properties();
            prop.load(input);

            String url = prop.getProperty("db.url");
            String user = prop.getProperty("db.user");
            String password = prop.getProperty("db.password");

            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            System.out.println("Connected to the database successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Displays the main menu for the security personnel.
     */
    public void showMenu() {
    	
    	int choice = 0;
        do {
            System.out.println("\nWelcome, Security Personnel!");
            System.out.println("-----------------------------------------------");
            System.out.println("1. Create Citation");
            System.out.println("2. Update Citation");
            System.out.println("3. View Citation");
            System.out.println("4. View all Citations");
            System.out.println("5. Delete Citation");
            System.out.println("6. Update Payment Status");
            System.out.println("7. Report Menu");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");
            
           choice = scanner.nextInt();

            switch (choice) {
                case 1:
                	createCitation();
                    break;
                case 2:
                    updateCitation();
                    break;
                case 3:
                	viewCitation(); 
                    break;
                case 4:
                	viewAllCitations();
                    break;
                case 5:
                	deleteCitation();
                    break;
                case 6:
                	updatePaymentStatus();
                	break;
                case 7:
                	reportMenu();
                	break;
                case 8:
                	System.out.println("Exiting. Goodbye!");       
					// LoginApplication.main(null);
                    break;
                default:
                    System.out.println("Invalid choice. Please try selecting from the Menu again");
            }
        } while (choice != 8);
    }
    
    /**
     * Displays the report menu for the security personnel.
     */
    public void reportMenu() {
    	int choice = 0;
        do {
            System.out.println("\nWelcome, to Report Menu!");
            System.out.println("-----------------------------------------------");
            System.out.println("1. Get Lot Citation Count");
            System.out.println("2. Get Zones");
            System.out.println("3. Get Violation Car Count");
            System.out.println("4. Get Permitted Employees");
            System.out.println("5. Get Permit Information");
            System.out.println("6. Get Available Spaces");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            
           choice = scanner.nextInt();

            switch (choice) {
                case 1:
                	scanner.nextLine();
                    System.out.print("Enter Start Time Range(YYYY-MM-DD): ");
                    String start = scanner.nextLine();
                    System.out.print("Enter End Time Range(YYYY-MM-DD): ");
                    String end = scanner.nextLine();
                	getLotCitationCount(start,end);
                    break;
                case 2:
                	getZones();
                    break;
                case 3:
                	getViolationCarCount();
                    break;
                case 4:
                	scanner.nextLine();
                    System.out.print("Enter Zone ID: ");
                    String p_id = scanner.nextLine();
                	getPermittedEmployeesCount(p_id);
                    break;
                case 5:
                	scanner.nextLine();
                    System.out.print("Enter User ID: ");
                    String u_id = scanner.nextLine();
                	getPermitInformation(u_id);
                    break;
                case 6:
                	scanner.nextLine();
                    System.out.print("Enter Parking ID: ");
                    String p_id_1 = scanner.nextLine();
                    System.out.print("Enter Space Type: ");
                    String space_type = scanner.nextLine();
                	getAvailableSpaces(p_id_1,space_type);
                	break;
                case 7:
                    System.out.println("Exiting. Goodbye!");     
                    break;
                default:
                    System.out.println("Invalid choice. Please try selecting from the Menu again");
                    break;
            }
        } while (choice != 7);
    }
    
    

    /**
     * Creates a new citation based on user input.
     */
    private void createCitation() {
        scanner.nextLine();
        System.out.print("Enter Citation ID: ");
        String id = scanner.nextLine();
        System.out.print("Enter carLicense Number: ");
        String carLicenseNo = scanner.nextLine();

        // Check if the car has a valid permit
        if (checkValidPermit(carLicenseNo)) {
            System.out.println("Car Permit not found or not expired. Citation cannot be created.");
            return;
        }

        System.out.print("Enter Parking Lot ID: ");
        String parkingLotId = scanner.nextLine();        
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String citationDate = currentDate.format(dateFormatter);
        String citationTime = currentTime.format(timeFormatter);
        System.out.print("Enter Category: ");
        String category = scanner.nextLine();
        System.out.print("Enter Fee Amount: ");
        String fee = scanner.nextLine();
        System.out.print("Enter the Payment Status: ");
        String paymentStatus = scanner.next();
        String sql = "INSERT INTO Citations (citationNumber, carLicenseNo, parkingLotID, citationDate, citationTime, category, fee, paymentStatus) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, carLicenseNo);
            pstmt.setString(3, parkingLotId);
            pstmt.setString(4, citationDate);
            pstmt.setString(5, citationTime);
            pstmt.setString(6, category);
            pstmt.setString(7, fee);
            pstmt.setString(8, paymentStatus);
            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("New Citation Has Been Created Successfully.");
                conn.commit();
            } else {
                System.out.println("Citation Creation Unsuccessful.");
                conn.rollback();
            }
        } catch (SQLException e) {  	
            System.out.println(e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
        }
    }

    /**
     * Checks if a car has a valid permit.
     *
     * @param carLicenseNo The license number of the car.
     * @return True if the car has a valid permit; otherwise, false.
     */
    private boolean checkValidPermit(String carLicenseNo) {
        String permitCheckQuery = "SELECT * FROM Permit WHERE carLicenseNo = ? AND expDate > CURRENT_DATE";
        
        try (PreparedStatement permitCheckStmt = conn.prepareStatement(permitCheckQuery)) {
            permitCheckStmt.setString(1, carLicenseNo);
            ResultSet resultSet = permitCheckStmt.executeQuery();
            
            return resultSet.next();
        } catch (SQLException e) {
            System.out.println("Error checking permit: " + e.getMessage());
            return false; 
        }
    }

    
    /**
     * Updates an existing citation based on user input.
     */
    private void updateCitation() {

    	scanner.nextLine();
        System.out.print("Enter Citation ID to update: ");
        String citationId = scanner.nextLine();
        System.out.println("Select the attribute to update:");
        System.out.println("-----------------------------------------------");
        System.out.println("1. Car License Number");
        System.out.println("2. Parking Lot ID");
        System.out.println("3. Category");
        System.out.println("4. Fee Amount");
        System.out.println("5. Appeal");
        System.out.print("Enter your choice (1-5): ");

        int choice = scanner.nextInt();
        scanner.nextLine(); 
        String newValue;
        switch (choice) {
            case 1:
                System.out.print("Enter new Car License Number: ");
                newValue = scanner.nextLine();
                updateAttribute(citationId, "carLicenseNo", newValue);
                break;
            case 2:
                System.out.print("Enter new Parking Lot ID: ");
                newValue = scanner.nextLine();
                updateAttribute(citationId, "parkingLotID", newValue);
                break;
            case 3:
                System.out.print("Enter new Category: ");
                newValue = scanner.nextLine();
                updateAttribute(citationId, "category", newValue);
                break;
            case 4:
                System.out.print("Enter new Fee Amount: ");
                newValue = scanner.nextLine();
                updateAttribute(citationId, "fee", newValue);
                break;
            case 5: 
            	System.out.print("Enter Appeal Yes/No: ");
                newValue = scanner.nextLine();
                updateAttribute(citationId, "appeal", newValue);
                break;
            default:
                System.out.println("Invalid choice. No updates performed.");
        }
    }

    /**
     * Updates a specific attribute of a citation.
     *
     * @param citationId     The ID of the citation to be updated.
     * @param attributeName  The name of the attribute to be updated.
     * @param newValue       The new value for the attribute.
     */
    private void updateAttribute(String citationId, String attributeName, String newValue) {
        String sql = "UPDATE Citations SET " + attributeName + " = ? WHERE citationNumber = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newValue);
            pstmt.setString(2, citationId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Citation updated successfully.");
                conn.commit();
            } else {
     
                System.out.println("No citation found with the given ID. Update unsuccessful.");
                conn.rollback();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
        }
    }
    
    /**
     * Displays all citations stored in the database.
     */
    private void viewAllCitations() {
        String sql = "SELECT * FROM Citations";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
            	System.out.println("Citation Number: " + rs.getString("citationNumber") +
                        ", Car License Number: " + rs.getString("carLicenseNo") +
                        ", Parking Lot ID: " + rs.getString("parkingLotID") +
                        ", Category: " + rs.getString("category") +
                        ", Fee: " + rs.getString("fee") +
                        ", Payment Status: " + rs.getString("paymentStatus") +
                        ", Appeal: " + rs.getString("appeal"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    /**
     * Displays the details of a specific citation based on user input.
     */
    private void viewCitation() {
    	scanner.nextLine();
        System.out.print("Enter Citation Number: ");
        String citationNumber = scanner.nextLine();

        String sql = "SELECT * FROM Citations WHERE citationNumber = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, citationNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Citation Number: " + rs.getString("citationNumber") +
                        ", Car License Number: " + rs.getString("carLicenseNo") +
                        ", Parking Lot ID: " + rs.getString("parkingLotID") +
                        ", Category: " + rs.getString("category") +
                        ", Fee: " + rs.getString("fee") +
                        ", Payment Status: " + rs.getString("paymentStatus") +
                        ", Appeal: " + rs.getString("appeal"));
            } else {
                System.out.println("Citation with number " + citationNumber + " not found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    /**
     * Deletes a specific citation based on user input.
     */
    private void deleteCitation() {
        scanner.nextLine();
        System.out.print("Enter Citation Number you want to delete: ");
        String citationNumber = scanner.nextLine();

        String sql = "DELETE FROM Citations WHERE citationNumber = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, citationNumber);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Citation has been deleted.");
                conn.commit();
            } else {
                System.out.println("Could not delete citation. Please check if the number is correct.");
                conn.rollback();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
        }
    }
    
    
    /**
     * Updates the payment status of a specific citation based on user input.
     */
    private void updatePaymentStatus() {
        scanner.nextLine();
        System.out.print("Enter Citation Number to update payment status: ");
        String citationNumber = scanner.nextLine();

        System.out.print("Enter new Payment Status: ");
        String newPaymentStatus = scanner.nextLine();

        String sql = "UPDATE Citations SET paymentStatus = ? WHERE citationNumber = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPaymentStatus);
            pstmt.setString(2, citationNumber);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Payment status updated successfully.");
                conn.commit();
            } else {
                System.out.println("No citation found with the given number. Update unsuccessful.");
                conn.rollback();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException rollbackException) {
                rollbackException.printStackTrace();
            }
        }
    }

    
    /**
     * Retrieves the citation count for each parking lot within a specified time range.
     *
     * @param startTime The start time of the range.
     * @param endTime   The end time of the range.
     */
    public void getLotCitationCount(String startTime, String endTime) {
        String sql;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(startTime, formatter);
        LocalDate endDate = LocalDate.parse(endTime, formatter);
        
        sql = "SELECT parkingLotID,COUNT(*) as citationCount FROM Citations WHERE citationDate BETWEEN '" + startDate + "' AND '" + endDate + "' GROUP BY parkingLotID";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String parkingLotID = rs.getString("parkingLotID");
                int count = rs.getInt("citationCount");
                
                System.out.println("Praking Lot:" + parkingLotID + "," + count + " citations");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Retrieves the zones information from the database.
     */

    public void getZones() {
        String sql = "SELECT parkingLotID, zoneID FROM Zone";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String parkingLotID = rs.getString("parkingLotID");
                String zone = rs.getString("zoneID");

                System.out.println("(" + parkingLotID + ", " + zone + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Retrieves the count of distinct car licenses with 'DUE' payment status.
     */
    public void getViolationCarCount() {
        String sql = "SELECT COUNT(DISTINCT carLicenseNo) as violationCount FROM Citations WHERE paymentStatus = 'DUE'";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String carCount = rs.getString("violationCount");
                System.out.println(carCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    

    /**
     * Retrieves the count of permitted employees for a specific zone.
     *
     * @param ZoneID The ID of the zone.
     */
    public void getPermittedEmployeesCount(String ZoneID) {
        if (ZoneID == null || ZoneID.isEmpty()) {
            throw new IllegalArgumentException("Zone ID cannot be null or empty.");
        }

        String sql = "SELECT COUNT(DISTINCT userID) as userCount FROM Permit WHERE zoneID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ZoneID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String userCount = rs.getString("userCount");
                System.out.println(userCount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Retrieves the permit information for a specific user ID.
     *
     * @param userID The user ID for which permit information is retrieved.
     */
    public void getPermitInformation(String userID) {
        if (userID == null || userID.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        }

        String sql = "SELECT * FROM Permit WHERE userID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String permitID = rs.getString("permitID");
                String parkingLotID = rs.getString("parkingLotID");
                // Add more fields as needed

                System.out.println("Permit ID: " + permitID);
                System.out.println("Parking Lot ID: " + parkingLotID);
                // Print additional permit information
            } else {
                System.out.println("No permit information found for user ID: " + userID);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    /**
     * Retrieves the available spaces for a specific parking lot and space type.
     *
     * @param parkingLotID The ID of the parking lot.
     * @param spaceType    The type of space.
     */

    public void getAvailableSpaces(String parkingLotID, String spaceType) {
        if (parkingLotID == null || parkingLotID.isEmpty() || spaceType == null || spaceType.isEmpty()) {
            throw new IllegalArgumentException("Parking Lot ID and Space Type cannot be null or empty.");
        }
        String availability = "Y";
        String sql = "SELECT spaceNumber FROM Space WHERE parkingLotID = ? AND spaceType = ? AND availability = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, parkingLotID);
            pstmt.setString(2, spaceType);
            pstmt.setString(3, availability);
            ResultSet rs = pstmt.executeQuery();

            List<String> availableSpaces = new ArrayList<>();

            while (rs.next()) {
                String spaceNumber = rs.getString("spaceNumber");
                availableSpaces.add(spaceNumber);
            }

            if (!availableSpaces.isEmpty()) {
                System.out.println("Available Spaces for Parking:");
                for (String space : availableSpaces) {
                    System.out.println(space);
                }
            } else {
                System.out.println("No available spaces found for the specified criteria.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
