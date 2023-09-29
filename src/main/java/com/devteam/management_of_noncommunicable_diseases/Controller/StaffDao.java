package com.devteam.management_of_noncommunicable_diseases.Controller;

import com.devteam.management_of_noncommunicable_diseases.Interface.InfoBox;
import com.devteam.management_of_noncommunicable_diseases.Interface.ShowAlert;
import com.devteam.management_of_noncommunicable_diseases.Model.Staff;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StaffDao implements InfoBox {
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Window owner;

    public void addStaff(Window owner, String username, String firstName, String lastName, String email, String idNumber, String phoneNumber, String passWord, String confirmPassword, String startWork) throws SQLException {
        String INSERT_ACCOUNTS_QUERY = "INSERT into accounts (user_name,password) VALUES (?,?)";
        String INSERT_STAFFS_QUERY = "INSERT into staffs (job_code,position,first_name,last_name,email,id_number,phone_number,start_work) VALUES (?,?,?,?,?,?,?,?)";
        String INSERT_DEPARTMENT_FACILITIES_QUERY = "INSERT INTO department_facilities (facility_id,department_id) VALUES (?,?)";
        String INSERT_DEPARTMENT_FACILITIES_INTO_STAFF_QUERY = "INSERT INTO staffs (department_facilities_id) VALUES (?)";

        boolean checkNameEmpty = validateEmptyFields(username, "Nhập tên đăng nhập", owner);
        boolean checkNameExisted = checkExistedItem(username, "accounts", "user_name", "Tên đã tồn tại", owner);
        boolean checkFirstName = validateEmptyFields(firstName, "Nhập tên", owner);
        boolean checkLastName = validateEmptyFields(lastName, "Nhập họ", owner);
        boolean checkEmailExist = checkExistedItem(email, "staffs", "email", "Email đã tồn tại", owner);
        boolean checkEmptyEmail = validateEmptyFields(email, "Nhập email", owner);
        boolean validOrInvalid = checkEmailValid(email, owner);
        boolean checkEmptyIdNumber = validateEmptyFields(idNumber, "Nhập số cccd", owner);
        boolean checkEmptyPhoneNumber = validateEmptyFields(phoneNumber, "Nhập số điện thoại", owner);
        boolean checkEmptyPassword = validateEmptyFields(passWord, "Nhập mật khẩu", owner);
        boolean checkEmptyConfirmPassword = validateEmptyFields(passWord, "Nhập mật khẩu", owner);
        boolean checkMatchingPass = checkMatchingPasswordAndLength(passWord, confirmPassword, owner);
        boolean checkExistingIdNumberAndLength = checkIdNumberAndLength(idNumber, "staffs", "ID đã tồn tại!", owner);

        if (checkNameExisted && checkNameEmpty && checkMatchingPass
                && checkFirstName && checkLastName && checkEmailExist
                && checkEmptyEmail && validOrInvalid && checkEmptyIdNumber
                && checkExistingIdNumberAndLength && checkEmptyPassword
                && checkEmptyConfirmPassword && checkEmptyPhoneNumber) {
            try {
                connection = DBConnection.open();
                assert connection != null;
                MD5 md5 = new MD5();
                String encodePassword = md5.encode(passWord);
                preparedStatement = connection.prepareStatement(INSERT_ACCOUNTS_QUERY);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, encodePassword);
                preparedStatement = connection.prepareStatement(INSERT_STAFFS_QUERY);
                preparedStatement.setString(1, null);
                preparedStatement.setString(2, null);
                preparedStatement.setString(3, firstName);
                preparedStatement.setString(4, lastName);
                preparedStatement.setString(5, email);
                preparedStatement.setString(6, idNumber);
                preparedStatement.setString(7, phoneNumber);
                preparedStatement.setDate(8, Date.valueOf(startWork));
                System.out.println(preparedStatement);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                DBConnection.closeAll(connection, preparedStatement, resultSet);
            }
        } else {
            new Thread(() -> {
                Platform.runLater(() -> {
                    InfoBox.infoBox("Thiếu thông tin,hãy kiểm tra lại", null, "Thất Bại...");
                });
            }).start();
        }
    }

//    public void updateStaff(Window owner) throws SQLException {
//
//        String FIND_SPECIFIC_STAFF = "SELECT id FROM staffs WHERE id = ?";
//        String QUERY_UPDATE_STAFF = "UPDATE staffs SET id_number = ?, email = ?, first_name = ?, last_name = ?, job_code = ?, phone_number = ? WHERE id = ?";
//        try {
//            connection = DBConnection.open();
//            assert connection != null;
//            preparedStatement = connection.prepareStatement(FIND_SPECIFIC_STAFF);
//            preparedStatement.setInt(1, staff.getId());
//            resultSet = preparedStatement.executeQuery();
//            while (resultSet.next()) {
//                String id_number = resultSet.getString("id_number");
//                String first_name = resultSet.getString("first_name");
//                String last_name = resultSet.getString("last_name");
//                String idOfUserInDb = resultSet.getString("phone_number");
//                String Email = resultSet.getString("email");
//                String job_code = resultSet.getString("job_code");
//            }
//            preparedStatement = connection.prepareStatement(QUERY_UPDATE_STAFF);
//            preparedStatement.setString(1, staff.getIdNumber());
//            preparedStatement.setString(2, staff.getEmail());
//            preparedStatement.setString(3, staff.getFirstName());
//            preparedStatement.setString(4, staff.getLastName());
//            preparedStatement.setString(5, staff.getJobCode());
//            preparedStatement.setString(6, staff.getPhoneNumber());
//
//            preparedStatement.executeUpdate();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            DBConnection.closeAll(connection, preparedStatement, resultSet);
//        }
//    }

    protected boolean validateEmptyFields(String dataField, String textToNotice, Window owner) {
        if (dataField.isEmpty()) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", textToNotice);
            return false;
        }
        return true;
    }

    protected boolean checkMatchingPasswordAndLength(String passWord, String confirmPassword, Window owner) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]{4,}");
        Matcher matcher = pattern.matcher(passWord);
        if (!Objects.equals(passWord, confirmPassword)) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Mật khẩu không khớp");
            return false;
        } else {
            if (!matcher.matches()) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Mật khẩu tối thiểu 4 ký tự");
                return false;
            }
        }
        return true;
    }

    protected boolean checkEmailValid(String Email, Window owner) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        Matcher matcher = pattern.matcher(Email);
        if (!matcher.matches()) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Định dạng email ko hợp lệ");
            return false;
        }
        return true;
    }

    public boolean checkIdNumberAndLength(String IdFromUserInput, String database, String textToNotice, Window owner) throws SQLException {
        String SELECT_ID_NUMBER_QUERY = "SELECT id_number FROM " + database + " WHERE id_number = ?";
        String IdInDatabase = null;
        connection = DBConnection.open();
        assert connection != null;
        preparedStatement = connection.prepareStatement(SELECT_ID_NUMBER_QUERY);
        preparedStatement.setInt(1, Integer.parseInt(IdFromUserInput));
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            IdInDatabase = resultSet.getString("id_number");
            Pattern pattern = Pattern.compile("[0-9]{12}");
            Matcher matcher = pattern.matcher(IdFromUserInput);
            if (!Objects.equals(IdFromUserInput, IdInDatabase)) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", textToNotice);
                return false;
            } else {
                if (!matcher.matches()) {
                    ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Độ dài không hợp lệ,Id phải đủ 12 số");
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkExistedItem(String itemFromUserInput, String databaseName, String columnName, String textToNotice, Window owner) throws SQLException {
        String SELECT_ITEM_QUERY = "SELECT " + columnName + " FROM " + databaseName + " WHERE " + columnName + " = ?";
        String itemInDatabase = null;
        connection = DBConnection.open();
        assert connection != null;
        preparedStatement = connection.prepareStatement(SELECT_ITEM_QUERY);
        preparedStatement.setString(1, itemFromUserInput);
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            itemInDatabase = resultSet.getString(columnName);
            if (Objects.equals(itemFromUserInput, itemInDatabase)) {
                ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", textToNotice);
                return false;
            }
        }
        return true;
    }
}
