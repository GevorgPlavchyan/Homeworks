package jdbc;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Scanner;

public interface Validation {
    LocalDate now = LocalDate.now();

    default int getIndex() {
        Scanner scanner = new Scanner(System.in);
        int index;
        try {
            index = scanner.nextInt();
            return index;
        } catch (NoSuchElementException | IllegalStateException e) {
            System.out.println("""

                     Enter only number!
                     Not a character or symbol!
                     """);

            return getIndex();
        }
    }

    default boolean isValidMail(String email) {
        return  email.endsWith("@gmail.com") || email.endsWith("@gmail.am") || email.endsWith("@gmail.ru") ||
                email.endsWith("@mail.com")  || email.endsWith("@mail.am")  || email.endsWith("@mail.ru")  ||
                email.endsWith("@yahoo.com") || email.endsWith("@yahoo.am") || email.endsWith("@yahoo.ru");
    }

    default String validMail() {
        Scanner scanner = new Scanner(System.in);
        String mail;
        try {
            mail = scanner.nextLine();
            if (isValidMail(mail)) {
                return mail;
            }
            System.out.println("Enter valid mail!");
            return validMail();
        } catch (NoSuchElementException | IllegalStateException e) {
            System.out.println("""

                     Enter only character!
                     Not a numbers or symbols!
                     """);
            return validMail();
        }
    }

    default boolean isValidName(String name) {
        if (name.isEmpty())
            return false;
        char[] chars = name.toCharArray();
        for (char c : chars)
            if (!Character.isLetter(c))
                return false;
        return true;
    }

    default String validName() {
        Scanner scanner = new Scanner(System.in);
        String name;
        try {
            name = scanner.nextLine();
            if (isValidName(name)) {
                return name;
            }
            throw new NoSuchElementException();
        } catch (NoSuchElementException | IllegalStateException e) {
            System.out.println("""

                     Enter only character!
                     Not a numbers or symbols!
                     """);
            return validName();
        }
    }

    default String validPhone() {
        Scanner scanner = new Scanner(System.in);
        String phone;
        try {
            phone = scanner.nextLine();
            if (isValidPhone(phone))
                return phone;
            System.out.println("Invalid Phone number!");
            return validPhone();
        } catch (NoSuchElementException | IllegalStateException e) {
            System.out.println("""

                     Enter only numbers or '-, +, *' symbols!
                     Not a characters or another symbols!
                     """);
            return validName();
        }
    }

    default boolean isValidPhone(String phone) {
        if (phone.isEmpty())
            return false;
        char[] chars = phone.toCharArray();
        for (char c : chars)
            if (!Character.isDigit(c) ||  c != '-' || c != '+' || c != '*')
                return false;
        return true;
    }

}
