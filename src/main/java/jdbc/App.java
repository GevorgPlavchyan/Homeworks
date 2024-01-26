package jdbc;

import java.sql.*;
import java.time.LocalDate;

public class App implements Validation {
    private final String url = "jdbc:postgresql://localhost:5432/bookstoredb";
    private final String userName = "postgres";
    private final String password = "885522";

    public App() {
        try (Connection connection = connect();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                                CREATE OR REPLACE FUNCTION update_book_date(
                                    book_id           INTEGER,
                                    title             VARCHAR,
                                    author            VARCHAR,
                                    genre             VARCHAR,
                                    price             REAL,
                                    quantity_in_stock INTEGER)
                                    RETURNS VOID AS $$
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
                             """);

            statement.execute("""
                                CREATE OR REPLACE FUNCTION update_customer_date(
                                    customer_id INTEGER,
                                    name        VARCHAR,
                                    email       VARCHAR,
                                    phone       VARCHAR)
                                    RETURNS VOID AS $$
                                        BEGIN
                                            UPDATE Customers
                                                SET Name = name,
                                                    Email = email,
                                                    Phone = phone
                                            WHERE CustomerID = customer_id;
                                        END;
                                    $$ LANGUAGE plpgsql;
                             """);

            statement.execute("""
                                CREATE OR REPLACE FUNCTION insert_sales_date(
                                    customer_id INTEGER,
                                    date        DATE,
                                    quantity    INTEGER)
                                    RETURNS VOID AS $$
                                        BEGIN
                                            INSERT INTO Seles (CustomerID, DateOfSale, QuantitySold)
                                            VALUES  (customer_id, date, quantity);
                                        END;
                                    $$ LANGUAGE plpgsql;
                             """);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    protected void updateBookData(int id, String title, String author, String genre, double price, int quantity) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            String s = String.format("Call update_book_date(%d, %s, %s, %s, %f, %d)", id, title, author, genre, price, quantity);
            connection.prepareCall(s);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    protected void booksByGenreOrAuthor() {
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

    protected void updateCustomerData(int id, String name, String email, String phone) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            String s = String.format("Call update_customer_date(%d, %s, %s, %s)", id, name, email, phone);
            connection.prepareCall(s);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    protected void customersPurchaseHistory() {
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

    protected void newSale(String bookName, String authorName, String customerName, String email, String phone) {
        try (Connection connection = DriverManager.getConnection(url, userName, password)) {
            int cs_id = getID(String.format("""
                                                        SELECT customer_id
                                                            FROM Customer AS c
                                                            WHERE c.name = %s
                                                    """, customerName));
            if (cs_id == -1) {
                System.out.println("Invalid customer!");
                return;
            }
            int bk_id = getID(String.format("""
                                                    SELECT book_id
                                                        FROM Books AS b
                                                        WHERE b.Title = %s ADN b.Author = %s
                                                """, bookName, authorName));
            if (bk_id == -1) {
                System.out.println("Invalid book!");
                return;
            }
            updateCustomerData(cs_id, customerName, email, phone);

            String s = "Call update_books_quantity_in_stock()";
            connection.prepareCall(s);

            s = String.format("Call insert_sales_date(%d, %d, %t)", bk_id, cs_id, LocalDate.now());
            connection.prepareCall(s);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
    }

    private int getID(String query) {
        int result = -1;
        try (Connection connection = DriverManager.getConnection(url, userName, password);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            if (!resultSet.wasNull())
                result = resultSet.getInt(1);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
        return result;
    }

    protected int salesTotalRevenueByGenre(String genre) {
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

    protected void salesHistory() {
        try (Connection connection = connect()) {
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

    protected void salesRevenueForEchGenre() {
        try (Connection connection = connect()) {
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

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, userName, password);
        } catch (SQLException r) {
            System.out.println("Invalid db" + r);
        }
        return conn;
    }
}
