package jdbc;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class CoreOfApp extends App implements Validation {

    private void updateBook() {
        updateBookData(getId(), getTitle(), getAuthor(), getGenre(), getPrice(), getQuantity());
    }

    private int getId() {
        System.out.println("Enter updating id!\n");
        return getIndex();
    }

    private int getQuantity() {
        System.out.println("Enter new quantity!\n");
        return getIndex();
    }

    private String getTitle() {
        System.out.println("Enter title of book!");
        return validName();
    }

    private String getAuthor() {
        System.out.println("Enter author of book!");
        return validName();
    }

    private String getGenre() {
        System.out.println("Enter genres of book!");
        return validName();
    }

    private double getPrice() {
        Scanner scanner = new Scanner(System.in);
        double price;
        try {
            System.out.println("Enter new price!\n");
            price = scanner.nextDouble();
            if (price >= 0.0)
                return price;
            System.out.println("Price can't be negative!");
            return getPrice();
        } catch (NoSuchElementException | IllegalStateException e) {
            System.out.println("""

                     Enter only numbers and point!
                     Not a characters or symbol!
                     """);
            return getPrice();
        }
    }

    private void updateCustomer() {
        updateCustomerData(getId(), getCustomerName(), getEmail(), getPhone());
    }

    private String getCustomerName() {
        System.out.println("Enter customer name!\n");
        return validName();
    }

    private String getEmail() {
        System.out.println("Enter customer name!\n");
        return validMail();
    }

    private String getPhone() {
        System.out.println("Enter customer name!\n");
        return validPhone();
    }

    public void work() {
        while (true) {
            System.out.println(toString());
            int index = getIndex();
            switch (index) {
                case 0 -> System.exit(0);
                case 1 -> updateBook();
                case 2 -> booksByGenreOrAuthor();
                case 3 -> updateCustomer();
                case 4 -> customersPurchaseHistory();
                case 5 -> newSale(getTitle(), getAuthor(), getCustomerName(), getEmail(), getPhone());
                case 6 -> salesTotalRevenueByGenre(getGenre());
                case 7 -> salesHistory();
                case 8 -> salesRevenueForEchGenre();
                default -> System.out.println("\nEnter valid value!");
            }
        }
    }

    public String toString() {
        return """
                                 
                Enter numbers
                1 -> for updating book date!
                2 -> list books by genre or author!
                3 -> for updating customer date!
                4 -> customer purchase history!
                5 -> add new sales!
                6 -> list total revenue by genre!
                7 -> list sales!
                8 -> list total revenue for each genre!
                0 -> exit from system!
                                 
                """;
    }
}
