package za.ac.cput.mylearning.servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class RegisterMyLearning extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter pw = response.getWriter();
        
        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Basic validation
        if (name == null || surname == null || email == null || password == null || confirmPassword == null ||
            name.trim().isEmpty() || surname.trim().isEmpty() || email.trim().isEmpty() ||
            password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            pw.println("<h3>Please fill all fields!</h3>");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            pw.println("<h3>Passwords do not match!</h3>");
            return;
        }

        try {
            // Load Derby driver (optional with modern JDBC, but kept for clarity)
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            Connection con = DriverManager.getConnection(
                "jdbc:derby://localhost:1527/MyLearningDataBase", "administration", "admin");
            
            // Use correct table and column names (assuming 'student' table exists)
            String sql = "INSERT INTO users (name,surname,email,password) VALUES (?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, surname);
                ps.setString(3, email);
                ps.setString(4, password);
                int i = ps.executeUpdate();
                
                if (i > 0) {
                    
                    response.sendRedirect("Dashboard.html");
                } else {
                    pw.println("<h3>Registration failed. Please try again.</h3>");
                }
            }
            con.close();
        } catch (ClassNotFoundException e) {
            pw.println("<h3>Error: Derby Driver not found - " + e.getMessage() + "</h3>");
            e.printStackTrace();
        } catch (SQLException e) {
            pw.println("<h3>Database Error: " + e.getMessage() + "</h3>");
            e.printStackTrace();
        }
    }
}