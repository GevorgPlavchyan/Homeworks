import java.sql.*;
import java.time.LocalDate;

public class App {
    private final String url = "jdbc:postgresql://localhost:5432/BookstoreDB";
    private final String userName = "postgres";
    private final String password = "postgres";

    public App() {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Statement statement = connection.createStatement()) {

            statement.execute("""
                                CREATE OR REPLACE FUNCTION update_book_date(
                                    book_id           INTEGER,
                                    title             TEXT,
                                    author            VARCHAR,
                                    genre             VARCHAR,
                                    price             INTEGER,
                                    quantity_in_stock INTEGER)
                                    RETURNS TRIGGER AS $$
                                        BEGIN
                                            UPDATE Books
                                                SET Title           = title,
                                                    Author          = author,
                                                    Genre           = genre,
                                                    Price           = price,
                                                    QuantityInStock = quantity_in_stock
                                            WHERE BookID        = book_id;
                                        END;
                                    $$ LANGUAGE plpgsql;
                                    
                                    CREATE TRIGGER update_book_id_from_sales
                                    AFTER INSERT ON Sales
                                    FOR EACH ROW
                                    EXECUTE FUNCTION update_book_date(book_id, title, author, genre, price, quantity_in_stock);
                                    
                             """);

            statement.execute("""
                                CREATE OR REPLACE FUNCTION update_customer_date(
                                    customer_id INTEGER,
                                    name        VARCHAR,
                                    email       VARCHAR,
                                    phone       VARCHAR)
                                    RETURNS TRIGGER AS $$
                                        BEGIN
                                            UPDATE Customers
                                                SET Name = name,
                                                    Email = email,
                                                    Phone = phone
                                            WHERE CustomerID = customer_id;
                                        END;
                                    $$ LANGUAGE plpgsql;
                                    
                                    CREATE TRIGGER update_customer_id_from_sales
                                    AFTER INSERT ON Sales
                                    FOR EACH ROW
                                    EXECUTE FUNCTION update_customer_date(customer_id, name, email, phone);
                             """);

            statement.execute("""
                                CREATE OR REPLACE FUNCTION insert_sales_date(
                                    sales_id    INTEGER,
                                    book_id     INTEGER,
                                    customer_id INTEGER,
                                    date        DATE,
                                    quantity    INTEGER)
                                    RETURNS TRIGGER AS $$
                                        BEGIN
                                            UPDATE Sales
                                                SET BookID       = book_id,
                                                    CustomerID   = customer_id,
                                                    DateOfSale   = date
//                                                    QuantitySold = quantity
                                            WHERE SalesID = sales_id;
                                        END;
                                    $$ LANGUAGE plpgsql;
                             """);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private void updateBookData(int id, String title, String author, String genre, int price, int quantity) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            String s = String.format("Call update_book_date(%d, %s, %s, %s, %d, %d)", id, title, author, genre, price, quantity);
            connection.prepareCall(s);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private void booksByGenreOrAuthor() {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                                SELECT b.title
                                    FROM Books AS b
                                    ORDER BY Author OR Genre;
                             """);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private void updateCustomerData(int id, String name, String email, String phone) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            String s = String.format("Call update_customer_date(%d, %s, %s, %s)", id, name, email, phone);
            connection.prepareCall(s);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private void customersPurchaseHistory() {
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                                SELECT  c.Name,
                                        c.Email,
                                        s.DateOfSale
                                    FROM Customers AS c
                                    INNER JOIN Seles AS s ON c.CustomerID = s.CustomerID;
                             """);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private void newSale(String bookName, String authorName, String customerName, String email, String phone) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            int cs_id = getMaxID("CustomerID") + 1;
            int sl_id = getMaxID("SalesID") + 1;
            int bk_id = getBookID(bookName, authorName);

            updateCustomerData(cs_id, customerName, email, phone);

            String s = "Call update_books_quantity_in_stock()";
            connection.prepareCall(s);

            s = String.format("Call insert_sales_date(%d, %d, %d, %t)", sl_id, bk_id, cs_id, LocalDate.now());
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private int getMaxID(String id) {
        int result = 0;
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Statement statement = connection.createStatement()) {
            String s = """
                          SELECT MAX(id)
                            FROM Customers
                       """;
            ResultSet resultSet = statement.executeQuery(s);
            if (!resultSet.wasNull())
                result = resultSet.getInt(1);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
        return result;
    }

    private int getBookID(String bookName, String authorName) {
        int result = 0;
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Statement statement = connection.createStatement()) {
            String s = String.format("""
                                        SELECT id
                                            FROM Books AS b
                                            WHERE b.Title = %s ADN b.Author = %s
                                    """, bookName, authorName);
            ResultSet resultSet = statement.executeQuery(s);
            if (!resultSet.wasNull())
                result = resultSet.getInt(1);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
        return result;
    }

    private int salesTotalRevenueByGenre(String genre) {
        int result = 0;
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Statement statement = connection.createStatement()) {
            String s = String.format("""
                                        SELECT SUM(s.TotalPrice) AS revenue
                                            FROM Sales AS s
                                            WHERE s.Genre = %s
                                     """, genre);
            ResultSet resultSet = statement.executeQuery(s);
            if (!resultSet.wasNull())
                result = resultSet.getInt(1);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
        return result;
    }

    private void salesHistory() {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            String s = """
                          SELECT s.DateOfSale AS date, b.Title AS book_tilte, c.Name AS name
                            FROM Sales AS s
                            JOIN Books AS b ON s.BookID = s.BookID
                            JOIN Customers AS c ON s.CustomerID = s.CustomerID;
                       """;
            connection.prepareCall(s);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private void salesRevenueForEchGenre() {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            String s = """
                          SELECT b.Genre AS genre, SUM(s.TotalPrice) AS revenue
                            FROM Sales AS s
                            JOIN Books AS b ON s.BookID = b.BookID
                            GROUP BY b.Genre;
                       """;
            connection.prepareCall(s);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    public void corOfApp() {}

}
