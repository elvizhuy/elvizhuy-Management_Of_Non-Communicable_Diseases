package com.devteam.management_of_noncommunicable_diseases.Controller;

import com.devteam.management_of_noncommunicable_diseases.Interface.InfoBox;
import com.devteam.management_of_noncommunicable_diseases.Interface.ShowAlert;
import com.devteam.management_of_noncommunicable_diseases.Model.Staff;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Window;
/*import jdk.nio.zipfs.ZipFileAttributeView;*/

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*import static jdk.nio.zipfs.ZipFileAttributeView.AttrID.owner;*/


public class StaffDao implements InfoBox,Runnable {
    Staff staff = new Staff();
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    Window owner;

    public void addStaff(Window owner) throws SQLException {
        String INSERT_ACCOUNTS_QUERY = "INSERT into accounts (user_name,password) VALUES (?,?)";
        String INSERT_STAFFS_QUERY = "INSERT into staffs (job_code,position,first_name,last_name,email,id_number,phone_number,start_work) VALUES (?,?,?,?,?,?,?,?)";
        String INSERT_DEPARTMENT_FACILITIES_QUERY = "INSERT INTO department_facilities (facility_id,department_id) VALUES (?,?)";
        String INSERT_DEPARTMENT_FACILITIES_INTO_STAFF_QUERY = "INSERT INTO staffs (department_facilities_id) VALUES (?)";
        String IdInDatabase = null;

        boolean checkName = validateEmptyFields(staff.getUserName(), "Nhập tên đăng nhập", owner);
        boolean checkFirstName = validateEmptyFields(staff.getFirstName(), "Nhập tên Họ", owner);
        boolean checkLastName = validateEmptyFields(staff.getLastName(), "Nhập tên", owner);
        boolean checkEmail = validateEmptyFields(staff.getEmail(), "Nhập email", owner);
        boolean checkIdNumber = validateEmptyFields(staff.getIdNumber(), "Nhập số cccd", owner);
        boolean checkPhoneNumber = validateEmptyFields(staff.getPhoneNumber(), "Nhập số điện thoại", owner);
        boolean checkPassword = validateEmptyFields(staff.getPassWord(), "Nhập mật khẩu", owner);
        boolean checkMatchingPass = checkMatchingPassword(staff.getPassWord(), staff.getConfirm_password(), "Mật khẩu không khớp!", owner);
        boolean checkExistingIdNumber = checkIdNumber(staff.getIdNumber(), null,"ID đã tồn tại!",owner);

        if (checkName && checkFirstName && checkLastName && checkEmail && checkIdNumber && checkPhoneNumber && checkPassword && checkMatchingPass && checkExistingIdNumber) {
            try {
                connection = DBConnection.open();
                assert connection != null;
                MD5 md5 = new MD5();
                String encodePassword = md5.encode(staff.getPassWord());
                preparedStatement = connection.prepareStatement(INSERT_ACCOUNTS_QUERY);
                preparedStatement.setString(1, staff.getUserName());
                preparedStatement.setString(2, encodePassword);
                preparedStatement = connection.prepareStatement(INSERT_STAFFS_QUERY);
                preparedStatement.setString(1, staff.getJobCode());
                preparedStatement.setString(2, staff.getPosition());
                preparedStatement.setString(3, staff.getFirstName());
                preparedStatement.setString(4, staff.getLastName());
                preparedStatement.setString(5, staff.getEmail());
                preparedStatement.setString(6, staff.getIdNumber());
                preparedStatement.setString(7, staff.getPhoneNumber());
                preparedStatement.setDate(8, Date.valueOf(staff.getStartWork()));
                System.out.println(preparedStatement);
                resultSet = preparedStatement.executeQuery();
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

    public void updateStaff(Window owner) throws SQLException {

        String FIND_SPECIFIC_STAFF = "SELECT id FROM staffs WHERE id = ?";
        String QUERY_UPDATE_STAFF = "UPDATE staffs SET id_number = ?, email = ?, first_name = ?, last_name = ?, job_code = ?, phone_number = ? WHERE id = ?";
        try {
            connection = DBConnection.open();
            assert connection != null;
            preparedStatement = connection.prepareStatement(FIND_SPECIFIC_STAFF);
            preparedStatement.setInt(1, staff.getId());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String id_number = resultSet.getString("id_number");
                String first_name = resultSet.getString("first_name");
                String last_name = resultSet.getString("last_name");
                String idOfUserInDb = resultSet.getString("phone_number");
                String Email = resultSet.getString("email");
                String job_code = resultSet.getString("job_code");
            }
            preparedStatement = connection.prepareStatement(QUERY_UPDATE_STAFF);
            preparedStatement.setString(1, staff.getIdNumber());
            preparedStatement.setString(2, staff.getEmail());
            preparedStatement.setString(3, staff.getFirstName());
            preparedStatement.setString(4, staff.getLastName());
            preparedStatement.setString(5, staff.getJobCode());
            preparedStatement.setString(6, staff.getPhoneNumber());

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DBConnection.closeAll(connection, preparedStatement, resultSet);
        }
    }

    protected boolean validateEmptyFields(String dataField, String textToNotice, Window owner) {
        if (dataField.isEmpty()) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", textToNotice);
            return false;
        }
        return true;
    }

    protected boolean checkMatchingPassword(String passWord, String confirmPassword, String textToNotice, Window owner) {
        if (!Objects.equals(passWord, confirmPassword)) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", textToNotice);
            return false;
        }
        return true;
    }

    public boolean checkIdNumber(String IdFromUserInput,String IdInDatabase ,String textToNotice, Window owner) throws SQLException {
        String SELECT_ID_NUMBER_QUERY = "SELECT id_number FROM staffs WHERE id_number = ?";
        connection = DBConnection.open();
        assert connection != null;
        preparedStatement = connection.prepareStatement(SELECT_ID_NUMBER_QUERY);
        preparedStatement.setInt(1, Integer.parseInt(staff.getIdNumber()));
        resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            IdInDatabase = resultSet.getString("id_number");
        }
        Pattern pattern = Pattern.compile("[0-9]{12}");
        Matcher matcher = pattern.matcher(IdFromUserInput);
        int ID = Integer.parseInt(IdFromUserInput);
        if (!Objects.equals(IdFromUserInput, IdInDatabase)) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", textToNotice);
            return false;
        }else {
            if (!matcher.find()){
                ShowAlert.showAlert(Alert.AlertType.ERROR, owner, "Form Error!", "Độ dài không hợp lệ,Id phải đủ 12 số");
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {
        try {
            addStaff(owner);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
