package space.yogeshwarank.satproject;

import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/users")
public class UserServlet extends HttpServlet {

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/db",
                "root", "password"
        );
    }

    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res)
            throws IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        out.println("<h2>User Management</h2>");
        out.println("<form method='post' action='users'>");
        out.println("ID (for update/delete): <input name='id'><br>");
        out.println("Name: <input name='name'><br>");
        out.println("Email: <input name='email'><br>");
        out.println("<button name='action' value='create'>Add</button>");
        out.println("<button name='action' value='update'>Update</button>");
        out.println("<button name='action' value='delete'>Delete</button>");
        out.println("</form><h3>Registered Users</h3>");

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                out.println(rs.getInt("id") + " - "
                        + rs.getString("name") + " - "
                        + rs.getString("email") + "<br>");
            }
        } catch (Exception e) {
            out.println("Database error.");
        }
    }

    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res)
            throws IOException {
        String action = req.getParameter("action");
        String id = req.getParameter("id");
        String name = req.getParameter("name");
        String email = req.getParameter("email");

        String sql;
        if ("create".equals(action)) {
            sql = "INSERT INTO users(name,email) VALUES(?,?)";
        } else if ("update".equals(action)) {
            sql = "UPDATE users SET name=?,email=? WHERE id=?";
        } else if ("delete".equals(action)) {
            sql = "DELETE FROM users WHERE id=?";
        } else {
            res.sendError(400, "Invalid action");
            return;
        }

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if ("create".equals(action)) {
                ps.setString(1, name);
                ps.setString(2, email);
            } else if ("update".equals(action)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setInt(3, Integer.parseInt(id));
            } else {
                ps.setInt(1, Integer.parseInt(id));
            }

            int rows = ps.executeUpdate();
            res.setContentType("text/html");
            res.getWriter().println(
                    rows > 0 ? "Operation successful."
                            : "No matching user found."
            );
            res.getWriter().println(
                    "<br><a href='users'>Back to User Management</a>"
            );

        } catch (Exception e) {
            res.sendError(500, "Database operation failed");
        }
    }
}