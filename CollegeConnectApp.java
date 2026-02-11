import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class CollegeConnectApp {

    // --- CONFIGURATION ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/college_connect_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; 

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Utility to clean Windows paths typed with quotes/backslashes
    private static String cleanPath(String p) {
        if (p == null) return null;
        return p.trim().replace("\"", "").replace("\\", "/");
    }

    // =========================================================================
    // 1. LOGIN SCREEN (NO STATE/COURSE HERE)
    // =========================================================================
    static class LoginFrame extends JFrame {
        JTextField txtUser;
        JPasswordField txtPass;

        public LoginFrame() {
            setTitle("College Connect Login");
            setSize(420, 280);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout(10, 10));

            add(new JLabel("Welcome to College Connect", SwingConstants.CENTER), BorderLayout.NORTH);

            JPanel pnl = new JPanel(new GridLayout(2, 2, 10, 10));
            pnl.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            pnl.add(new JLabel("Username:"));
            txtUser = new JTextField();
            pnl.add(txtUser);

            pnl.add(new JLabel("Password:"));
            txtPass = new JPasswordField();
            pnl.add(txtPass);

            add(pnl, BorderLayout.CENTER);

            JPanel btns = new JPanel(new FlowLayout());
            JButton btnLogin = new JButton("Login");
            JButton btnReg = new JButton("Register");
            btns.add(btnLogin);
            btns.add(btnReg);
            add(btns, BorderLayout.SOUTH);

            btnLogin.addActionListener(e -> login());
            btnReg.addActionListener(e -> register());

            setVisible(true);
        }

        void login() {
            try (Connection con = getConnection()) {
                if (con == null) return;

                PreparedStatement ps = con.prepareStatement(
                        "SELECT role FROM users WHERE username=? AND password=?"
                );
                ps.setString(1, txtUser.getText().trim());
                ps.setString(2, new String(txtPass.getPassword()));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    dispose();
                    if ("Admin".equalsIgnoreCase(rs.getString("role"))) {
                        new AdminDashboard();
                    } else {
                        new StudentDashboard(txtUser.getText().trim());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Credentials");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Login error: " + e.getMessage());
            }
        }

        void register() {
            String u = JOptionPane.showInputDialog(this, "New Username:");
            if (u == null || u.trim().isEmpty()) return;

            String p = JOptionPane.showInputDialog(this, "New Password:");
            if (p == null || p.trim().isEmpty()) return;

            try (Connection con = getConnection()) {
                if (con == null) return;

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (username, password, role) VALUES (?, ?, 'Student')"
                );
                ps.setString(1, u.trim());
                ps.setString(2, p);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Registered!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Register error: " + e.getMessage());
            }
        }
    }

    // =========================================================================
    // 2. STUDENT DASHBOARD (FILTER BY STATE + COURSE + SEARCH + COMPARE)
    // =========================================================================
    static class StudentDashboard extends JFrame {
        String user;
        JTable table;
        DefaultTableModel model;

        JComboBox<String> cmbState;
        JComboBox<String> cmbCourse;
        JTextField txtSearch;

        public StudentDashboard(String user) {
            this.user = user;

            setTitle("Student Dashboard - " + user);
            setSize(1200, 720);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Top Panel
            JPanel top = new JPanel();

            cmbState = new JComboBox<>(new String[]{
                    "All States",
                    "Andhra Pradesh", "Telangana", "Karnataka", "Tamil Nadu", "Kerala",
                    "Maharashtra", "Gujarat", "Rajasthan", "Uttar Pradesh", "Madhya Pradesh",
                    "West Bengal", "Odisha", "Punjab", "Haryana", "Bihar", "Assam", "Delhi"
            });

            cmbCourse = new JComboBox<>(new String[]{
                    "All Courses",
                    "B.Tech", "M.Tech", "MBA", "BBA", "B.Sc", "M.Sc", "BCA", "MCA", "MBBS", "B.Com", "M.Com"
            });

            txtSearch = new JTextField(15);

            JButton btnFilter = new JButton("Apply Filter");
            JButton btnClear = new JButton("Clear");
            JButton btnView = new JButton("View Full Profile");
            JButton btnCompare = new JButton("Compare");
            JButton btnLogout = new JButton("Logout");

            top.add(new JLabel("State:"));
            top.add(cmbState);
            top.add(new JLabel("Course:"));
            top.add(cmbCourse);

            top.add(new JLabel("Search:"));
            top.add(txtSearch);

            top.add(btnFilter);
            top.add(btnClear);
            top.add(btnView);
            top.add(btnCompare);
            top.add(btnLogout);

            add(top, BorderLayout.NORTH);

            // Table
            model = new DefaultTableModel(new String[]{"ID", "Name", "State", "Course", "Location", "Fees"}, 0);
            table = new JTable(model);

            // multi-select for compare
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            add(new JScrollPane(table), BorderLayout.CENTER);

            // Load all initially
            loadColleges();

            btnFilter.addActionListener(e -> loadColleges());
            btnClear.addActionListener(e -> {
                cmbState.setSelectedIndex(0);
                cmbCourse.setSelectedIndex(0);
                txtSearch.setText("");
                loadColleges();
            });

            btnView.addActionListener(e -> openCollegeProfile());
            btnCompare.addActionListener(e -> openCompareDialog());

            btnLogout.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(
                        this, "Do you want to logout?", "Confirm Logout",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginFrame();
                }
            });

            setVisible(true);
        }

        void loadColleges() {
            model.setRowCount(0);

            String selectedState = (String) cmbState.getSelectedItem();
            String selectedCourse = (String) cmbCourse.getSelectedItem();
            String search = txtSearch.getText().trim();

            // Build query with optional filters
            StringBuilder sql = new StringBuilder(
                    "SELECT college_id, name, state, course, location, fees FROM colleges WHERE 1=1"
            );

            // we'll use PreparedStatement
            // params will be added only when filter is applied
            try (Connection con = getConnection()) {
                if (con == null) return;

                java.util.ArrayList<Object> params = new java.util.ArrayList<>();

                if (selectedState != null && !"All States".equals(selectedState)) {
                    sql.append(" AND state = ?");
                    params.add(selectedState);
                }

                if (selectedCourse != null && !"All Courses".equals(selectedCourse)) {
                    sql.append(" AND course = ?");
                    params.add(selectedCourse);
                }

                if (!search.isEmpty()) {
                    sql.append(" AND name LIKE ?");
                    params.add("%" + search + "%");
                }

                sql.append(" ORDER BY college_id ASC");

                PreparedStatement ps = con.prepareStatement(sql.toString());

                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("college_id"),
                            rs.getString("name"),
                            rs.getString("state"),
                            rs.getString("course"),
                            rs.getString("location"),
                            rs.getDouble("fees")
                    });
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Load colleges error: " + e.getMessage());
            }
        }

        void openCollegeProfile() {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a college first!");
                return;
            }
            int id = (int) model.getValueAt(row, 0);
            new CollegeProfileDialog(this, id, user);
        }

        void openCompareDialog() {
            int[] rows = table.getSelectedRows();
            if (rows == null || rows.length < 2) {
                JOptionPane.showMessageDialog(this, "Select at least 2 colleges to compare! (Ctrl + Click)");
                return;
            }

            int[] ids = new int[rows.length];
            for (int i = 0; i < rows.length; i++) {
                ids[i] = (int) model.getValueAt(rows[i], 0);
            }

            new CompareCollegesDialog(this, ids);
        }
    }

    // =========================================================================
    // 3. DETAILED COLLEGE PROFILE
    // =========================================================================
    static class CollegeProfileDialog extends JDialog {
        int collegeId;
        String studentName;

        public CollegeProfileDialog(JFrame parent, int id, String studentName) {
            super(parent, "College Profile", true);
            this.collegeId = id;
            this.studentName = studentName;

            setSize(900, 700);
            setLocationRelativeTo(parent);

            JTabbedPane tabs = new JTabbedPane();

            JPanel pnlOverview = new JPanel(new BorderLayout());
            JPanel pnlInfo = new JPanel();
            pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));

            JLabel lblImage = new JLabel();
            lblImage.setPreferredSize(new Dimension(800, 250));

            JTextArea txtDesc = new JTextArea(10, 50);
            txtDesc.setEditable(false);

            JButton btnApply = new JButton("Apply for Admission");
            btnApply.setFont(new Font("Arial", Font.BOLD, 16));
            btnApply.setBackground(Color.ORANGE);

            try (Connection con = getConnection()) {
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement(
                            "SELECT name, image_path, description, fees, state, course FROM colleges WHERE college_id=?"
                    );
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();

                    if (rs.next()) {
                        setTitle(rs.getString("name"));

                        String imgPath = cleanPath(rs.getString("image_path"));
                        if (imgPath != null && !imgPath.isEmpty() && new File(imgPath).exists()) {
                            ImageIcon icon = new ImageIcon(
                                    new ImageIcon(imgPath).getImage().getScaledInstance(
                                            600, 200, Image.SCALE_SMOOTH
                                    )
                            );
                            lblImage.setIcon(icon);
                        } else {
                            lblImage.setText("No College Image");
                            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                        }

                        txtDesc.setText(
                                "About College:\n" + rs.getString("description") +
                                "\n\nState: " + rs.getString("state") +
                                "\nCourse: " + rs.getString("course") +
                                "\n\nAnnual Fees: Rs. " + rs.getDouble("fees")
                        );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            pnlInfo.add(lblImage);
            pnlInfo.add(new JScrollPane(txtDesc));
            pnlInfo.add(btnApply);
            pnlOverview.add(pnlInfo, BorderLayout.CENTER);

            DefaultTableModel facModel = new DefaultTableModel(new String[]{"Facility Name"}, 0);
            JTable facTable = new JTable(facModel);

            DefaultTableModel hosModel = new DefaultTableModel(new String[]{"Room Type", "Fee (per year)"}, 0);
            JTable hosTable = new JTable(hosModel);

            JPanel pnlPlace = new JPanel(new GridLayout(0, 3, 10, 10));
            loadExtraDetails(facModel, hosModel, pnlPlace);

            tabs.addTab("Overview", pnlOverview);
            tabs.addTab("Facilities", new JScrollPane(facTable));
            tabs.addTab("Hostels", new JScrollPane(hosTable));
            tabs.addTab("Placements", new JScrollPane(pnlPlace));

            add(tabs);

            btnApply.addActionListener(e -> {
                try (Connection con = getConnection()) {
                    if (con == null) return;
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO applications (student_name, college_name) VALUES (?, ?)"
                    );
                    ps.setString(1, studentName);
                    ps.setString(2, getTitle());
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Application Submitted Successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });

            setVisible(true);
        }

        void loadExtraDetails(DefaultTableModel fac, DefaultTableModel hos, JPanel pnlPlace) {
            try (Connection con = getConnection()) {
                if (con == null) return;

                PreparedStatement ps = con.prepareStatement(
                        "SELECT facility_name FROM facilities WHERE college_id=?"
                );
                ps.setInt(1, collegeId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) fac.addRow(new Object[]{rs.getString("facility_name")});

                ps = con.prepareStatement(
                        "SELECT type, fee FROM hostels WHERE college_id=?"
                );
                ps.setInt(1, collegeId);
                rs = ps.executeQuery();
                while (rs.next()) hos.addRow(new Object[]{rs.getString("type"), rs.getDouble("fee")});

                ps = con.prepareStatement(
                        "SELECT student_name, company, package_lpa, student_image_path FROM placements WHERE college_id=?"
                );
                ps.setInt(1, collegeId);
                rs = ps.executeQuery();

                while (rs.next()) {
                    JPanel card = new JPanel(new BorderLayout());
                    card.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                    String path = cleanPath(rs.getString("student_image_path"));
                    JLabel img = new JLabel("No Photo", SwingConstants.CENTER);

                    if (path != null && !path.isEmpty() && new File(path).exists()) {
                        ImageIcon scaled = new ImageIcon(
                                new ImageIcon(path).getImage().getScaledInstance(
                                        100, 100, Image.SCALE_SMOOTH
                                )
                        );
                        img.setIcon(scaled);
                        img.setText("");
                    }

                    String info = "<html><b>" + rs.getString("student_name") + "</b><br>" +
                            "Placed in: " + rs.getString("company") + "<br>" +
                            "Pkg: " + rs.getDouble("package_lpa") + " LPA</html>";

                    card.add(img, BorderLayout.CENTER);
                    card.add(new JLabel(info, SwingConstants.CENTER), BorderLayout.SOUTH);
                    pnlPlace.add(card);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // =========================================================================
    // COMPARE COLLEGES DIALOG
    // =========================================================================
    static class CompareCollegesDialog extends JDialog {
        int[] collegeIds;

        public CompareCollegesDialog(JFrame parent, int[] collegeIds) {
            super(parent, "Compare Colleges", true);
            this.collegeIds = collegeIds;

            setSize(1200, 650);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout(10, 10));

            DefaultTableModel cmpModel = new DefaultTableModel(
                    new String[]{
                            "College",
                            "State",
                            "Course",
                            "Location",
                            "Fees",
                            "Facilities",
                            "Hostels",
                            "Placements (Placed/Avg/Max/Top)"
                    }, 0
            );

            JTable cmpTable = new JTable(cmpModel);
            cmpTable.setRowHeight(85);

            loadComparison(cmpModel);

            add(new JScrollPane(cmpTable), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(close);
            add(bottom, BorderLayout.SOUTH);

            close.addActionListener(e -> dispose());
            setVisible(true);
        }

        void loadComparison(DefaultTableModel cmpModel) {
            cmpModel.setRowCount(0);

            try (Connection con = getConnection()) {
                if (con == null) return;

                for (int id : collegeIds) {

                    // Basic
                    String name = "", location = "", state = "", course = "";
                    double fees = 0;

                    PreparedStatement ps = con.prepareStatement(
                            "SELECT name, location, fees, state, course FROM colleges WHERE college_id=?"
                    );
                    ps.setInt(1, id);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        name = rs.getString("name");
                        location = rs.getString("location");
                        fees = rs.getDouble("fees");
                        state = rs.getString("state");
                        course = rs.getString("course");
                    }

                    // Facilities
                    StringBuilder facilitiesList = new StringBuilder();
                    int facilitiesCount = 0;

                    ps = con.prepareStatement("SELECT facility_name FROM facilities WHERE college_id=?");
                    ps.setInt(1, id);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        facilitiesCount++;
                        facilitiesList.append("• ").append(rs.getString("facility_name")).append("<br>");
                    }
                    String facilitiesHtml = "<html><b>" + facilitiesCount + "</b><br>" +
                            (facilitiesCount == 0 ? "No data" : facilitiesList.toString()) + "</html>";

                    // Hostels
                    StringBuilder hostelsList = new StringBuilder();
                    int hostelCount = 0;

                    ps = con.prepareStatement("SELECT type, fee FROM hostels WHERE college_id=?");
                    ps.setInt(1, id);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        hostelCount++;
                        hostelsList.append("• ").append(rs.getString("type"))
                                .append(" : ₹").append(rs.getDouble("fee"))
                                .append("<br>");
                    }
                    String hostelsHtml = "<html>" + (hostelCount == 0 ? "No data" : hostelsList.toString()) + "</html>";

                    // Placements summary
                    int placedCount = 0;
                    double sumPkg = 0;
                    double maxPkg = 0;

                    ps = con.prepareStatement("SELECT company, package_lpa FROM placements WHERE college_id=?");
                    ps.setInt(1, id);
                    rs = ps.executeQuery();

                    java.util.LinkedHashSet<String> topCompanies = new java.util.LinkedHashSet<>();

                    while (rs.next()) {
                        placedCount++;
                        double pkg = rs.getDouble("package_lpa");
                        sumPkg += pkg;
                        if (pkg > maxPkg) maxPkg = pkg;

                        String company = rs.getString("company");
                        if (company != null && !company.trim().isEmpty() && topCompanies.size() < 5) {
                            topCompanies.add(company.trim());
                        }
                    }

                    double avgPkg = (placedCount == 0) ? 0 : (sumPkg / placedCount);
                    String companiesStr = topCompanies.isEmpty() ? "No data" : String.join(", ", topCompanies);

                    String placementsHtml = "<html>" +
                            "<b>Placed:</b> " + placedCount + "<br>" +
                            "<b>Avg:</b> " + String.format("%.2f", avgPkg) + " LPA<br>" +
                            "<b>Max:</b> " + String.format("%.2f", maxPkg) + " LPA<br>" +
                            "<b>Top:</b> " + companiesStr +
                            "</html>";

                    cmpModel.addRow(new Object[]{
                            name, state, course, location, "₹" + fees, facilitiesHtml, hostelsHtml, placementsHtml
                    });
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Compare Error: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // ADMIN DASHBOARD
    // =========================================================================
    static class AdminDashboard extends JFrame {
        public AdminDashboard() {
            setTitle("Admin Dashboard");
            setSize(1200, 720);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setLayout(new BorderLayout());

            JTabbedPane tabs = new JTabbedPane();
            tabs.addTab("Add College", new AddCollegePanel());
            tabs.addTab("Add Details", new AddDetailsPanel());
            tabs.addTab("Manage Colleges", new ManageCollegesPanel());

            JButton btnLogout = new JButton("Logout");
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(btnLogout);

            add(tabs, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            btnLogout.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(
                        this, "Do you want to logout?", "Confirm Logout",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginFrame();
                }
            });

            setVisible(true);
        }
    }

    // =========================================================================
    // ADMIN: ADD COLLEGE (NOW includes State + Course)
    // =========================================================================
    static class AddCollegePanel extends JPanel {
        JTextField name, loc, fees, imgPath, state, course;
        JTextArea desc;

        public AddCollegePanel() {
            setLayout(new GridLayout(8, 2, 8, 8));

            add(new JLabel("Name:")); name = new JTextField(); add(name);
            add(new JLabel("State:")); state = new JTextField(); add(state);
            add(new JLabel("Course:")); course = new JTextField(); add(course);

            add(new JLabel("Location:")); loc = new JTextField(); add(loc);
            add(new JLabel("Fees:")); fees = new JTextField(); add(fees);
            add(new JLabel("Image Path (e.g C:/img.jpg):")); imgPath = new JTextField(); add(imgPath);
            add(new JLabel("Description:")); desc = new JTextArea(); add(new JScrollPane(desc));

            JButton btn = new JButton("Save College");
            add(btn);
            add(new JLabel(""));

            btn.addActionListener(e -> {
                try (Connection con = getConnection()) {
                    if (con == null) return;

                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO colleges (name, state, course, location, fees, image_path, description) VALUES (?,?,?,?,?,?,?)"
                    );
                    ps.setString(1, name.getText().trim());
                    ps.setString(2, state.getText().trim());
                    ps.setString(3, course.getText().trim());
                    ps.setString(4, loc.getText().trim());
                    ps.setDouble(5, Double.parseDouble(fees.getText().trim()));
                    ps.setString(6, cleanPath(imgPath.getText()));
                    ps.setString(7, desc.getText().trim());

                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Saved!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                }
            });
        }
    }

    // =========================================================================
    // ADMIN: ADD DETAILS (Facilities + Hostels + Placements)
    // =========================================================================
    static class AddDetailsPanel extends JPanel {
        JTextField id;
        JTextField fac;
        JTextField hosType, hosFee;
        JTextField studName, comp, pkg, studImg;

        public AddDetailsPanel() {
            setLayout(new GridLayout(14, 2));

            add(new JLabel("College ID (Check Database):")); id = new JTextField(); add(id);

            add(new JLabel("--- Add Facility ---")); add(new JLabel(""));
            add(new JLabel("Facility Name:")); fac = new JTextField(); add(fac);
            JButton btnFac = new JButton("Add Facility"); add(btnFac); add(new JLabel(""));

            add(new JLabel("--- Add Hostel ---")); add(new JLabel(""));
            add(new JLabel("Hostel Type:")); hosType = new JTextField(); add(hosType);
            add(new JLabel("Hostel Fee (per year):")); hosFee = new JTextField(); add(hosFee);
            JButton btnHos = new JButton("Add Hostel"); add(btnHos); add(new JLabel(""));

            add(new JLabel("--- Add Placement ---")); add(new JLabel(""));
            add(new JLabel("Student Name:")); studName = new JTextField(); add(studName);
            add(new JLabel("Company:")); comp = new JTextField(); add(comp);
            add(new JLabel("Package (LPA):")); pkg = new JTextField(); add(pkg);
            add(new JLabel("Student Image Path:")); studImg = new JTextField(); add(studImg);
            JButton btnPlace = new JButton("Add Placement"); add(btnPlace); add(new JLabel(""));

            btnFac.addActionListener(e -> addFacility());
            btnHos.addActionListener(e -> addHostel());
            btnPlace.addActionListener(e -> addPlacement());
        }

        private int getCollegeIdOrWarn() {
            String cid = id.getText().trim();
            if (cid.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter College ID (number).");
                return -1;
            }
            try {
                return Integer.parseInt(cid);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "College ID must be a number!");
                return -1;
            }
        }

        void addFacility() {
            int cid = getCollegeIdOrWarn();
            if (cid == -1) return;

            String facility = fac.getText().trim();
            if (facility.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Facility Name.");
                return;
            }

            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO facilities (college_id, facility_name) VALUES (?, ?)"
                );
                ps.setInt(1, cid);
                ps.setString(2, facility);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Facility Added!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void addHostel() {
            int cid = getCollegeIdOrWarn();
            if (cid == -1) return;

            String type = hosType.getText().trim();
            String feeStr = hosFee.getText().trim();
            if (type.isEmpty() || feeStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter Hostel Type and Fee.");
                return;
            }

            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO hostels (college_id, type, fee) VALUES (?, ?, ?)"
                );
                ps.setInt(1, cid);
                ps.setString(2, type);
                ps.setDouble(3, Double.parseDouble(feeStr));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Hostel Added!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void addPlacement() {
            int cid = getCollegeIdOrWarn();
            if (cid == -1) return;

            String student = studName.getText().trim();
            String company = comp.getText().trim();
            String packStr = pkg.getText().trim();
            String imgPath = cleanPath(studImg.getText());

            if (student.isEmpty() || company.isEmpty() || packStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill Student, Company and Package.");
                return;
            }

            try (Connection con = getConnection()) {
                if (con == null) return;

                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO placements (college_id, student_name, company, package_lpa, student_image_path) VALUES (?, ?, ?, ?, ?)"
                );
                ps.setInt(1, cid);
                ps.setString(2, student);
                ps.setString(3, company);
                ps.setDouble(4, Double.parseDouble(packStr));
                ps.setString(5, imgPath);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Placement Added!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // ADMIN: MANAGE COLLEGES (UPDATE + DELETE) including State/Course
    // =========================================================================
    static class ManageCollegesPanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        JTextField txtId, txtName, txtState, txtCourse, txtLoc, txtFees, txtImg;
        JTextArea txtDesc;

        public ManageCollegesPanel() {
            setLayout(new BorderLayout(10, 10));

            model = new DefaultTableModel(new String[]{"ID", "Name", "State", "Course", "Location", "Fees"}, 0);
            table = new JTable(model);

            loadColleges();
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
            txtId = new JTextField(); txtId.setEditable(false);
            txtName = new JTextField();
            txtState = new JTextField();
            txtCourse = new JTextField();
            txtLoc = new JTextField();
            txtFees = new JTextField();
            txtImg = new JTextField();
            txtDesc = new JTextArea(3, 20);

            form.add(new JLabel("College ID:")); form.add(txtId);
            form.add(new JLabel("Name:")); form.add(txtName);
            form.add(new JLabel("State:")); form.add(txtState);
            form.add(new JLabel("Course:")); form.add(txtCourse);
            form.add(new JLabel("Location:")); form.add(txtLoc);
            form.add(new JLabel("Fees:")); form.add(txtFees);
            form.add(new JLabel("Image Path:")); form.add(txtImg);
            form.add(new JLabel("Description:")); form.add(new JScrollPane(txtDesc));

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton btnRefresh = new JButton("Refresh");
            JButton btnUpdate = new JButton("Update");
            JButton btnDelete = new JButton("Delete");

            buttons.add(btnRefresh);
            buttons.add(btnUpdate);
            buttons.add(btnDelete);

            JPanel south = new JPanel(new BorderLayout());
            south.add(form, BorderLayout.CENTER);
            south.add(buttons, BorderLayout.SOUTH);

            add(south, BorderLayout.SOUTH);

            table.getSelectionModel().addListSelectionListener(e -> {
                if (e.getValueIsAdjusting()) return;
                int row = table.getSelectedRow();
                if (row == -1) return;
                int id = (int) model.getValueAt(row, 0);
                loadCollegeToForm(id);
            });

            btnRefresh.addActionListener(e -> loadColleges());
            btnUpdate.addActionListener(e -> updateCollege());
            btnDelete.addActionListener(e -> deleteCollege());
        }

        void loadColleges() {
            model.setRowCount(0);
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                        "SELECT college_id, name, state, course, location, fees FROM colleges ORDER BY college_id DESC"
                );
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("college_id"),
                            rs.getString("name"),
                            rs.getString("state"),
                            rs.getString("course"),
                            rs.getString("location"),
                            rs.getDouble("fees")
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void loadCollegeToForm(int id) {
            try (Connection con = getConnection()) {
                if (con == null) return;

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM colleges WHERE college_id=?"
                );
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    txtId.setText(String.valueOf(rs.getInt("college_id")));
                    txtName.setText(rs.getString("name"));
                    txtState.setText(rs.getString("state"));
                    txtCourse.setText(rs.getString("course"));
                    txtLoc.setText(rs.getString("location"));
                    txtFees.setText(String.valueOf(rs.getDouble("fees")));
                    txtImg.setText(rs.getString("image_path"));
                    txtDesc.setText(rs.getString("description"));
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void updateCollege() {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select a college row first!");
                return;
            }

            try (Connection con = getConnection()) {
                if (con == null) return;

                PreparedStatement ps = con.prepareStatement(
                        "UPDATE colleges SET name=?, state=?, course=?, location=?, fees=?, image_path=?, description=? WHERE college_id=?"
                );
                ps.setString(1, txtName.getText().trim());
                ps.setString(2, txtState.getText().trim());
                ps.setString(3, txtCourse.getText().trim());
                ps.setString(4, txtLoc.getText().trim());
                ps.setDouble(5, Double.parseDouble(txtFees.getText().trim()));
                ps.setString(6, cleanPath(txtImg.getText()));
                ps.setString(7, txtDesc.getText().trim());
                ps.setInt(8, Integer.parseInt(txtId.getText().trim()));

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Updated Successfully!");
                loadColleges();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void deleteCollege() {
            if (txtId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select a college row first!");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Delete this college?\nThis may also delete related facilities/hostels/placements.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) return;

            try (Connection con = getConnection()) {
                if (con == null) return;

                int collegeId = Integer.parseInt(txtId.getText().trim());

                PreparedStatement ps = con.prepareStatement("DELETE FROM facilities WHERE college_id=?");
                ps.setInt(1, collegeId);
                ps.executeUpdate();

                ps = con.prepareStatement("DELETE FROM hostels WHERE college_id=?");
                ps.setInt(1, collegeId);
                ps.executeUpdate();

                ps = con.prepareStatement("DELETE FROM placements WHERE college_id=?");
                ps.setInt(1, collegeId);
                ps.executeUpdate();

                ps = con.prepareStatement("DELETE FROM colleges WHERE college_id=?");
                ps.setInt(1, collegeId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Deleted Successfully!");
                txtId.setText(""); txtName.setText(""); txtState.setText(""); txtCourse.setText("");
                txtLoc.setText(""); txtFees.setText(""); txtImg.setText(""); txtDesc.setText("");

                loadColleges();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
