import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CollegeConnectApp {

    // --- CONFIGURATION ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/college_connect_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // Change this if your MySQL password is not 'root'

    public static void main(String[] args) {
        // Modern L&F + subtle theme tweaks
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            Font f = new Font("Segoe UI", Font.PLAIN, 13);
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object val = UIManager.get(key);
                if (val instanceof javax.swing.plaf.FontUIResource) {
                    UIManager.put(key, new javax.swing.plaf.FontUIResource(f));
                }
            }

            UIManager.put("control", new Color(248, 250, 252));
            UIManager.put("nimbusBase", new Color(99, 102, 241));
            UIManager.put("nimbusBlueGrey", new Color(226, 232, 240));
            UIManager.put("text", new Color(15, 23, 42));
        } catch (Exception ignored) {}

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
    // THEME (UI ONLY)
    // =========================================================================
    static class Theme {
        static final Color BG = new Color(248, 250, 252);
        static final Color CARD = Color.WHITE;
        static final Color TEXT = new Color(15, 23, 42);
        static final Color MUTED = new Color(100, 116, 139);
        static final Color BORDER = new Color(226, 232, 240);
        static final Color HEADER = new Color(241, 245, 249);

        static final Color PRIMARY = new Color(99, 102, 241);
        static final Color PRIMARY_DARK = new Color(79, 70, 229);

        static final Color ACCENT = new Color(16, 185, 129);
        static final Color ACCENT_DARK = new Color(5, 150, 105);

        static final Color WARN = new Color(245, 158, 11);
        static final Color WARN_DARK = new Color(217, 119, 6);

        static final Color DANGER = new Color(239, 68, 68);
        static final Color DANGER_DARK = new Color(220, 38, 38);

        static final Color SLATE = new Color(100, 116, 139);
        static final Color SLATE_DARK = new Color(71, 85, 105);

        static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
        static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
        static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);

        static void applyFrame(JFrame f) {
            f.getContentPane().setBackground(BG);
        }

        static void applyDialog(JDialog d) {
            d.getContentPane().setBackground(BG);
        }

        static class ShadowBorder extends javax.swing.border.AbstractBorder {
            private final int pad = 10;
            @Override public Insets getBorderInsets(Component c) { return new Insets(pad, pad, pad, pad); }
            @Override public Insets getBorderInsets(Component c, Insets insets) {
                insets.left = insets.right = insets.top = insets.bottom = pad;
                return insets;
            }
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 0; i < 8; i++) {
                    g2.setColor(new Color(15, 23, 42, 12 - i));
                    g2.drawRoundRect(x + i, y + i, w - i * 2 - 1, h - i * 2 - 1, 18, 18);
                }
                g2.dispose();
            }
        }

        static void styleCard(JPanel p) {
            p.setBackground(CARD);
            p.setBorder(BorderFactory.createCompoundBorder(
                    new ShadowBorder(),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(BORDER),
                            BorderFactory.createEmptyBorder(18, 18, 18, 18)
                    )
            ));
        }

        static void styleLabel(JLabel l) {
            l.setForeground(TEXT);
            l.setFont(FONT_NORMAL);
        }

        static void styleTitle(JLabel l) {
            l.setForeground(TEXT);
            l.setFont(FONT_TITLE);
        }

        static void styleField(JTextField t) {
            t.setFont(FONT_NORMAL);
            t.setBackground(Color.WHITE);
            t.setForeground(TEXT);
            t.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
        }

        static void styleTextArea(JTextArea a) {
            a.setFont(FONT_NORMAL);
            a.setForeground(TEXT);
            a.setBackground(Color.WHITE);
            a.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }

        static void styleCombo(JComboBox<?> c) {
            c.setFont(FONT_NORMAL);
        }

        static void styleButton(JButton b, Color bg, Color hoverBg) {
            b.setFont(FONT_BOLD);
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setOpaque(true);
            b.setBorderPainted(false);
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
            b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

            b.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { b.setBackground(hoverBg); }
                @Override public void mouseExited(MouseEvent e) { b.setBackground(bg); }
            });
        }
        
        static JButton createCircularProfileButton(String username) {
            String initial = username != null && !username.isEmpty() ? username.substring(0, 1).toUpperCase() : "?";
            
            JButton btn = new JButton(initial) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isRollover()) {
                        g2.setColor(PRIMARY_DARK);
                    } else {
                        g2.setColor(PRIMARY);
                    }
                    g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                    super.paintComponent(g);
                    g2.dispose();
                }
                
                @Override
                protected void paintBorder(Graphics g) {
                }
            };
            
            btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
            btn.setForeground(Color.WHITE);
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setToolTipText("My Profile");
            
            return btn;
        }

        static void styleTable(JTable table) {
            table.setFont(FONT_NORMAL);
            table.setRowHeight(28);
            table.setShowHorizontalLines(true);
            table.setShowVerticalLines(false);
            table.setGridColor(BORDER);
            table.setSelectionBackground(new Color(219, 234, 254));
            table.setSelectionForeground(TEXT);

            if (table.getTableHeader() != null) {
                table.getTableHeader().setFont(FONT_BOLD);
                table.getTableHeader().setBackground(HEADER);
                table.getTableHeader().setForeground(TEXT);
            }

            DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                    return c;
                }
            };
            table.setDefaultRenderer(Object.class, r);
        }

        static void styleTabs(JTabbedPane tabs) {
            tabs.setFont(FONT_BOLD);
            tabs.setBackground(BG);
        }

        static JScrollPane styleScroll(JScrollPane sp) {
            sp.setBorder(BorderFactory.createLineBorder(BORDER));
            return sp;
        }

        static JPanel appBar(String title, String subtitle, JPanel rightPanel) {
            JPanel bar = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    GradientPaint gp = new GradientPaint(0, 0, PRIMARY, getWidth(), 0, ACCENT);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
            };
            bar.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

            JLabel t = new JLabel(title);
            t.setForeground(Color.WHITE);
            t.setFont(new Font("Segoe UI", Font.BOLD, 18));

            JLabel s = new JLabel(subtitle);
            s.setForeground(new Color(245, 245, 255));
            s.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JPanel left = new JPanel();
            left.setOpaque(false);
            left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            left.add(t);
            left.add(Box.createVerticalStrut(4));
            left.add(s);

            bar.add(left, BorderLayout.WEST);
            
            if (rightPanel != null) {
                rightPanel.setOpaque(false);
                bar.add(rightPanel, BorderLayout.EAST);
            }
            
            return bar;
        }
    }

    // =========================================================================
    // 1. LOGIN SCREEN
    // =========================================================================
    static class LoginFrame extends JFrame {
        JTextField txtUser;
        JPasswordField txtPass;

        public LoginFrame() {
            setTitle("College Connect Login");
            setSize(760, 420);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            Theme.applyFrame(this);
            add(Theme.appBar("College Connect", "Secure login • Smart filtering • Compare colleges", null), BorderLayout.NORTH);

            JPanel wrapper = new JPanel(new GridBagLayout());
            wrapper.setBackground(Theme.BG);
            add(wrapper, BorderLayout.CENTER);

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            Theme.styleCard(card);
            card.setPreferredSize(new Dimension(520, 280));

            JLabel title = new JLabel("Welcome Back 👋", SwingConstants.CENTER);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            Theme.styleTitle(title);

            JLabel sub = new JLabel("Login to explore colleges and apply quickly", SwingConstants.CENTER);
            sub.setAlignmentX(Component.CENTER_ALIGNMENT);
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            sub.setForeground(Theme.MUTED);

            card.add(title);
            card.add(Box.createVerticalStrut(6));
            card.add(sub);
            card.add(Box.createVerticalStrut(18));

            JPanel form = new JPanel(new GridLayout(2, 2, 10, 14));
            form.setOpaque(false);

            JLabel lblUser = new JLabel("Username:");
            Theme.styleLabel(lblUser);

            txtUser = new JTextField();
            Theme.styleField(txtUser);
            txtUser.setPreferredSize(new Dimension(260, 30));
            JPanel userWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            userWrap.setOpaque(false);
            userWrap.add(txtUser);

            JLabel lblPass = new JLabel("Password:");
            Theme.styleLabel(lblPass);

            txtPass = new JPasswordField();
            Theme.styleField(txtPass);
            txtPass.setPreferredSize(new Dimension(260, 30));
            JPanel passWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            passWrap.setOpaque(false);
            passWrap.add(txtPass);

            form.add(lblUser);
            form.add(userWrap);
            form.add(lblPass);
            form.add(passWrap);

            card.add(form);
            card.add(Box.createVerticalStrut(10));

            JCheckBox showPass = new JCheckBox("Show Password");
            showPass.setOpaque(false);
            showPass.setFont(Theme.FONT_NORMAL);
            showPass.setForeground(Theme.MUTED);
            showPass.setAlignmentX(Component.CENTER_ALIGNMENT);
            showPass.addActionListener(e -> txtPass.setEchoChar(showPass.isSelected() ? (char) 0 : '•'));
            card.add(showPass);

            card.add(Box.createVerticalStrut(14));

            JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            btns.setOpaque(false);

            JButton btnLogin = new JButton("Login");
            JButton btnReg = new JButton("Register");
            Theme.styleButton(btnLogin, Theme.PRIMARY, Theme.PRIMARY_DARK);
            Theme.styleButton(btnReg, Theme.ACCENT, Theme.ACCENT_DARK);

            btnLogin.addActionListener(e -> login());
            btnReg.addActionListener(e -> register());

            btns.add(btnLogin);
            btns.add(btnReg);

            card.add(btns);
            wrapper.add(card);

            setVisible(true);
        }

        void login() {
            try (Connection con = getConnection()) {
                if (con == null) return;

                PreparedStatement ps = con.prepareStatement(
                        "SELECT role, college_id FROM users WHERE username=? AND password=?"
                );
                ps.setString(1, txtUser.getText().trim());
                ps.setString(2, new String(txtPass.getPassword()));
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    dispose();
                    String role = rs.getString("role");
                    
                    if ("Admin".equalsIgnoreCase(role)) {
                        new AdminDashboard();
                    } else if ("College".equalsIgnoreCase(role)) {
                        int collegeId = rs.getInt("college_id");
                        new CollegeDashboard(txtUser.getText().trim(), collegeId);
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
    // 2. STUDENT DASHBOARD 
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
            setSize(1250, 740);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            Theme.applyFrame(this);

            JPanel rightMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightMenu.setOpaque(false);
            
            JButton btnProfile = Theme.createCircularProfileButton(user);
            JButton btnLogout = new JButton("Logout");
            Theme.styleButton(btnLogout, Theme.DANGER, Theme.DANGER_DARK); 
            
            rightMenu.add(btnProfile);
            rightMenu.add(btnLogout);

            JPanel north = new JPanel(new BorderLayout());
            north.setBackground(Theme.BG);
            north.add(Theme.appBar("Student Dashboard", "Check the boxes to compare • View Profiles • Apply", rightMenu), BorderLayout.NORTH);

            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
            top.setBackground(Theme.BG);

            cmbState = new JComboBox<>(new String[]{
                    "All States",
                    "Andhra Pradesh", "Telangana", "Karnataka", "Tamil Nadu", "Kerala",
                    "Maharashtra", "Gujarat", "Rajasthan", "Uttar Pradesh", "Madhya Pradesh",
                    "West Bengal", "Odisha", "Punjab", "Haryana", "Bihar", "Assam", "Delhi"
            });
            Theme.styleCombo(cmbState);

            cmbCourse = new JComboBox<>(new String[]{
                    "All Courses",
                    "B.Tech", "M.Tech", "MBA", "BBA", "B.Sc", "M.Sc", "BCA", "MCA", "MBBS", "B.Com", "M.Com"
            });
            Theme.styleCombo(cmbCourse);

            txtSearch = new JTextField(16);
            Theme.styleField(txtSearch);

            txtSearch.setForeground(Theme.MUTED);
            txtSearch.setText("Search college name...");
            txtSearch.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) {
                    if ("Search college name...".equals(txtSearch.getText())) {
                        txtSearch.setText("");
                        txtSearch.setForeground(Theme.TEXT);
                    }
                }
                @Override public void focusLost(FocusEvent e) {
                    if (txtSearch.getText().trim().isEmpty()) {
                        txtSearch.setText("Search college name...");
                        txtSearch.setForeground(Theme.MUTED);
                    }
                }
            });

            JButton btnFilter = new JButton("Apply Filter");
            JButton btnClear = new JButton("Clear");
            JButton btnView = new JButton("View Full Profile");
            JButton btnCompare = new JButton("Compare Selected");

            Theme.styleButton(btnFilter, Theme.PRIMARY, Theme.PRIMARY_DARK);
            Theme.styleButton(btnClear, Theme.SLATE, Theme.SLATE_DARK);
            Theme.styleButton(btnView, Theme.ACCENT, Theme.ACCENT_DARK);
            Theme.styleButton(btnCompare, Theme.WARN, Theme.WARN_DARK);

            JLabel s1 = new JLabel("State:");
            JLabel s2 = new JLabel("Course:");
            JLabel s3 = new JLabel("Search:");
            Theme.styleLabel(s1); Theme.styleLabel(s2); Theme.styleLabel(s3);

            top.add(s1); top.add(cmbState);
            top.add(s2); top.add(cmbCourse);
            top.add(s3); top.add(txtSearch);
            top.add(btnFilter); top.add(btnClear);
            top.add(btnView); top.add(btnCompare); 

            north.add(top, BorderLayout.SOUTH);
            add(north, BorderLayout.NORTH);

            model = new DefaultTableModel(new String[]{"Compare", "ID", "Name", "State", "Course", "Location", "Fees"}, 0) {
                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) return Boolean.class; 
                    return super.getColumnClass(columnIndex);
                }
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column == 0; 
                }
            };
            
            table = new JTable(model);
            Theme.styleTable(table);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            table.getColumnModel().getColumn(0).setMaxWidth(70);
            table.getColumnModel().getColumn(1).setMaxWidth(50);

            add(Theme.styleScroll(new JScrollPane(table)), BorderLayout.CENTER);

            loadColleges();

            btnFilter.addActionListener(e -> loadColleges());
            btnClear.addActionListener(e -> {
                cmbState.setSelectedIndex(0);
                cmbCourse.setSelectedIndex(0);
                txtSearch.setText("Search college name...");
                txtSearch.setForeground(Theme.MUTED);
                loadColleges();
            });

            btnView.addActionListener(e -> openCollegeProfile());
            btnCompare.addActionListener(e -> openCompareDialog());
            
            btnProfile.addActionListener(e -> new StudentProfileDialog(this, user));
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
            if ("Search college name...".equals(search)) search = "";

            StringBuilder sql = new StringBuilder(
                    "SELECT college_id, name, state, course, location, fees FROM colleges WHERE 1=1"
            );

            try (Connection con = getConnection()) {
                if (con == null) return;

                ArrayList<Object> params = new ArrayList<>();

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
                for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            false, 
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
                JOptionPane.showMessageDialog(this, "Click on a college row first to view its profile!");
                return;
            }
            int id = (int) model.getValueAt(row, 1); 
            new CollegeProfileDialog(this, id, user);
        }

        void openCompareDialog() {
            List<Integer> selectedIds = new ArrayList<>();
            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean isChecked = (Boolean) model.getValueAt(i, 0);
                if (isChecked != null && isChecked) {
                    selectedIds.add((Integer) model.getValueAt(i, 1)); 
                }
            }

            if (selectedIds.size() < 2) {
                JOptionPane.showMessageDialog(this, "Please check the 'Compare' box for at least 2 colleges!");
                return;
            }

            int[] ids = new int[selectedIds.size()];
            for (int i = 0; i < selectedIds.size(); i++) ids[i] = selectedIds.get(i);

            new CompareCollegesDialog(this, ids);
        }
    }

    // =========================================================================
    // 3. STUDENT PROFILE DIALOG
    // =========================================================================
    static class StudentProfileDialog extends JDialog {
        String username;
        JTextField txtFull, txtEmail, txtPhone, txtPass;

        public StudentProfileDialog(JFrame parent, String username) {
            super(parent, "My Profile", true);
            this.username = username;
            
            setSize(480, 420);
            setLocationRelativeTo(parent);
            Theme.applyDialog(this);

            add(Theme.appBar("My Profile", "Update your personal details", null), BorderLayout.NORTH);

            try (Connection con = getConnection(); Statement st = con.createStatement()) {
                st.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS full_name VARCHAR(100)");
                st.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100)");
                st.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20)");
            } catch (Exception ignored) { }

            JPanel card = new JPanel(new GridLayout(5, 2, 10, 15));
            Theme.styleCard(card);

            JLabel l1 = new JLabel("Username (Read-only):"); Theme.styleLabel(l1);
            JTextField txtUser = new JTextField(username); 
            Theme.styleField(txtUser); 
            txtUser.setEditable(false); 
            txtUser.setBackground(Theme.BORDER);
            txtUser.setForeground(Color.BLACK);
            
            JLabel l2 = new JLabel("Full Name:"); Theme.styleLabel(l2);
            txtFull = new JTextField(); Theme.styleField(txtFull);
            
            JLabel l3 = new JLabel("Email Address:"); Theme.styleLabel(l3);
            txtEmail = new JTextField(); Theme.styleField(txtEmail);
            
            JLabel l4 = new JLabel("Phone Number:"); Theme.styleLabel(l4);
            txtPhone = new JTextField(); Theme.styleField(txtPhone);
            
            JLabel l5 = new JLabel("Password:"); Theme.styleLabel(l5);
            txtPass = new JTextField(); Theme.styleField(txtPass);

            card.add(l1); card.add(txtUser);
            card.add(l2); card.add(txtFull);
            card.add(l3); card.add(txtEmail);
            card.add(l4); card.add(txtPhone);
            card.add(l5); card.add(txtPass);

            loadProfileData();

            JButton btnSave = new JButton("Save Changes");
            Theme.styleButton(btnSave, Theme.PRIMARY, Theme.PRIMARY_DARK);
            
            JButton btnCancel = new JButton("Cancel");
            Theme.styleButton(btnCancel, Theme.SLATE, Theme.SLATE_DARK);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.setBackground(Theme.BG);
            bottom.add(btnCancel);
            bottom.add(btnSave);

            add(card, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> saveProfileData());

            setVisible(true);
        }

        void loadProfileData() {
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                    "SELECT full_name, email, phone, password FROM users WHERE username=?"
                );
                ps.setString(1, username);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtFull.setText(rs.getString("full_name") != null ? rs.getString("full_name") : "");
                    txtEmail.setText(rs.getString("email") != null ? rs.getString("email") : "");
                    txtPhone.setText(rs.getString("phone") != null ? rs.getString("phone") : "");
                    txtPass.setText(rs.getString("password") != null ? rs.getString("password") : "");
                }
            } catch (Exception ex) {
                System.err.println("Load profile error: " + ex.getMessage());
            }
        }

        void saveProfileData() {
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET full_name=?, email=?, phone=?, password=? WHERE username=?"
                );
                ps.setString(1, txtFull.getText().trim());
                ps.setString(2, txtEmail.getText().trim());
                ps.setString(3, txtPhone.getText().trim());
                ps.setString(4, txtPass.getText().trim());
                ps.setString(5, username);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Profile Updated Successfully!");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating profile: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // 4. DETAILED COLLEGE PROFILE
    // =========================================================================
    static class CollegeProfileDialog extends JDialog {
        int collegeId;
        String studentName;

        public CollegeProfileDialog(JFrame parent, int id, String studentName) {
            super(parent, "College Profile", true);
            this.collegeId = id;
            this.studentName = studentName;

            setSize(980, 720);
            setLocationRelativeTo(parent);

            Theme.applyDialog(this);

            JPanel north = new JPanel(new BorderLayout());
            north.setBackground(Theme.BG);
            north.add(Theme.appBar("College Profile", "Overview • Facilities • Hostels • Placements", null), BorderLayout.NORTH);
            add(north, BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            Theme.styleTabs(tabs);

            JPanel pnlOverview = new JPanel(new BorderLayout());
            pnlOverview.setBackground(Theme.BG);

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            Theme.styleCard(card);

            JLabel lblImage = new JLabel();
            lblImage.setPreferredSize(new Dimension(860, 250));
            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
            lblImage.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

            JTextArea txtDesc = new JTextArea(10, 50);
            txtDesc.setEditable(false);
            txtDesc.setLineWrap(true);
            txtDesc.setWrapStyleWord(true);
            Theme.styleTextArea(txtDesc);

            JButton btnApply = new JButton("Apply for Admission");
            Theme.styleButton(btnApply, Theme.WARN, Theme.WARN_DARK);

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
                                            820, 240, Image.SCALE_SMOOTH
                                    )
                            );
                            lblImage.setIcon(icon);
                            lblImage.setText("");
                        } else {
                            lblImage.setIcon(null);
                            lblImage.setText("No College Image");
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

            card.add(lblImage);
            card.add(Box.createVerticalStrut(10));
            card.add(Theme.styleScroll(new JScrollPane(txtDesc)));
            card.add(Box.createVerticalStrut(12));

            JPanel applyRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            applyRow.setOpaque(false);
            applyRow.add(btnApply);
            card.add(applyRow);

            pnlOverview.add(card, BorderLayout.CENTER);

            DefaultTableModel facModel = new DefaultTableModel(new String[]{"Facility Name"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable facTable = new JTable(facModel);
            Theme.styleTable(facTable);

            DefaultTableModel hosModel = new DefaultTableModel(new String[]{"Room Type", "Fee (per year)"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            JTable hosTable = new JTable(hosModel);
            Theme.styleTable(hosTable);

            JPanel pnlPlace = new JPanel(new GridLayout(0, 3, 12, 12));
            pnlPlace.setBackground(Theme.BG);
            loadExtraDetails(facModel, hosModel, pnlPlace);

            tabs.addTab("Overview", pnlOverview);
            tabs.addTab("Facilities", Theme.styleScroll(new JScrollPane(facTable)));
            tabs.addTab("Hostels", Theme.styleScroll(new JScrollPane(hosTable)));
            tabs.addTab("Placements", Theme.styleScroll(new JScrollPane(pnlPlace)));

            add(tabs, BorderLayout.CENTER);

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
                    Theme.styleCard(card);

                    String path = cleanPath(rs.getString("student_image_path"));
                    JLabel img = new JLabel("No Photo", SwingConstants.CENTER);
                    img.setPreferredSize(new Dimension(120, 120));
                    img.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
                    img.setForeground(Theme.MUTED);

                    if (path != null && !path.isEmpty() && new File(path).exists()) {
                        ImageIcon scaled = new ImageIcon(
                                new ImageIcon(path).getImage().getScaledInstance(
                                        110, 110, Image.SCALE_SMOOTH
                                )
                        );
                        img.setIcon(scaled);
                        img.setText("");
                    }

                    String info = "<html><b>" + rs.getString("student_name") + "</b><br>" +
                            "Placed in: " + rs.getString("company") + "<br>" +
                            "Pkg: " + rs.getDouble("package_lpa") + " LPA</html>";

                    JLabel infoLbl = new JLabel(info, SwingConstants.CENTER);
                    infoLbl.setFont(Theme.FONT_NORMAL);
                    infoLbl.setForeground(Theme.TEXT);

                    card.add(img, BorderLayout.CENTER);
                    card.add(infoLbl, BorderLayout.SOUTH);

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

            Theme.applyDialog(this);
            add(Theme.appBar("Compare Colleges", "Side-by-side comparison of fees, hostels, facilities & placements", null), BorderLayout.NORTH);

            DefaultTableModel cmpModel = new DefaultTableModel(
                    new String[]{
                            "College", "State", "Course", "Location", "Fees",
                            "Facilities", "Hostels", "Placements (Placed/Avg/Max/Top)"
                    }, 0
            ) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };

            JTable cmpTable = new JTable(cmpModel);
            Theme.styleTable(cmpTable);
            cmpTable.setRowHeight(90);

            loadComparison(cmpModel);

            add(Theme.styleScroll(new JScrollPane(cmpTable)), BorderLayout.CENTER);

            JButton close = new JButton("Close");
            Theme.styleButton(close, Theme.SLATE, Theme.SLATE_DARK);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.setBackground(Theme.BG);
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

            Theme.applyFrame(this);

            JPanel rightMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightMenu.setOpaque(false);
            JButton btnLogout = new JButton("Logout");
            Theme.styleButton(btnLogout, Theme.DANGER, Theme.DANGER_DARK);
            rightMenu.add(btnLogout);

            add(Theme.appBar("Admin Dashboard", "Add colleges • Add details • Manage & update data", rightMenu), BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            Theme.styleTabs(tabs);

            tabs.addTab("Add College", new AddCollegePanel());
            tabs.addTab("Add Details", new AddDetailsPanel());
            tabs.addTab("Manage Colleges", new ManageCollegesPanel());
            tabs.addTab("View Applications", new ViewApplicationsPanel());

            add(tabs, BorderLayout.CENTER);

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
    // ADMIN VIEW APPLICATIONS PANEL
    // =========================================================================
    static class ViewApplicationsPanel extends JPanel {
        DefaultTableModel model;
        JTable table;

        public ViewApplicationsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            try (Connection con = getConnection(); Statement st = con.createStatement()) {
                st.execute("ALTER TABLE applications ADD COLUMN id INT AUTO_INCREMENT PRIMARY KEY FIRST");
            } catch (Exception ignored) {}

            model = new DefaultTableModel(new String[]{"Application ID", "Student Username", "College Applied To"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(model);
            Theme.styleTable(table);

            loadApplications();

            JButton btnRefresh = new JButton("Refresh Applications");
            Theme.styleButton(btnRefresh, Theme.PRIMARY, Theme.PRIMARY_DARK);
            btnRefresh.addActionListener(e -> loadApplications());

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setBackground(Theme.BG);
            topPanel.add(btnRefresh);

            add(topPanel, BorderLayout.NORTH);
            add(Theme.styleScroll(new JScrollPane(table)), BorderLayout.CENTER);
        }

        void loadApplications() {
            model.setRowCount(0);
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement(
                        "SELECT id, student_name, college_name FROM applications ORDER BY id DESC"
                );
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("student_name"),
                            rs.getString("college_name")
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading applications: " + ex.getMessage());
            }
        }
    }

    // =========================================================================
    // ADMIN: ADD COLLEGE 
    // =========================================================================
    static class AddCollegePanel extends JPanel {
        JTextField name, loc, fees, imgPath, state, course;
        JTextArea desc;

        public AddCollegePanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            JPanel card = new JPanel(new GridLayout(8, 2, 10, 12));
            Theme.styleCard(card);

            JLabel l1 = new JLabel("Name:");
            JLabel l2 = new JLabel("State:");
            JLabel l3 = new JLabel("Course:");
            JLabel l4 = new JLabel("Location:");
            JLabel l5 = new JLabel("Fees:");
            JLabel l6 = new JLabel("Image Path (e.g C:/img.jpg):");
            JLabel l7 = new JLabel("Description:");
            Theme.styleLabel(l1); Theme.styleLabel(l2); Theme.styleLabel(l3);
            Theme.styleLabel(l4); Theme.styleLabel(l5); Theme.styleLabel(l6); Theme.styleLabel(l7);

            card.add(l1); name = new JTextField(); Theme.styleField(name); card.add(name);
            card.add(l2); state = new JTextField(); Theme.styleField(state); card.add(state);
            card.add(l3); course = new JTextField(); Theme.styleField(course); card.add(course);
            card.add(l4); loc = new JTextField(); Theme.styleField(loc); card.add(loc);
            card.add(l5); fees = new JTextField(); Theme.styleField(fees); card.add(fees);
            card.add(l6); imgPath = new JTextField(); Theme.styleField(imgPath); card.add(imgPath);

            desc = new JTextArea(4, 20);
            desc.setLineWrap(true);
            desc.setWrapStyleWord(true);
            Theme.styleTextArea(desc);

            card.add(l7);
            card.add(Theme.styleScroll(new JScrollPane(desc)));

            JButton btn = new JButton("Save College");
            Theme.styleButton(btn, Theme.PRIMARY, Theme.PRIMARY_DARK);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.setBackground(Theme.BG);
            bottom.add(btn);

            add(card, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);

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
    // ADMIN: ADD DETAILS 
    // =========================================================================
    static class AddDetailsPanel extends JPanel {
        JTextField id, fac, hosType, hosFee, studName, comp, pkg, studImg;

        public AddDetailsPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            JPanel card = new JPanel(new GridLayout(14, 2, 10, 12));
            Theme.styleCard(card);

            JLabel a = new JLabel("College ID (Check Database):"); Theme.styleLabel(a);
            card.add(a); id = new JTextField(); Theme.styleField(id); card.add(id);

            JLabel b1 = new JLabel("--- Add Facility ---"); Theme.styleLabel(b1);
            card.add(b1); card.add(new JLabel(""));

            JLabel b2 = new JLabel("Facility Name:"); Theme.styleLabel(b2);
            card.add(b2); fac = new JTextField(); Theme.styleField(fac); card.add(fac);

            JButton btnFac = new JButton("Add Facility");
            Theme.styleButton(btnFac, Theme.PRIMARY, Theme.PRIMARY_DARK);
            card.add(btnFac); card.add(new JLabel(""));

            JLabel c1 = new JLabel("--- Add Hostel ---"); Theme.styleLabel(c1);
            card.add(c1); card.add(new JLabel(""));

            JLabel c2 = new JLabel("Hostel Type:"); Theme.styleLabel(c2);
            card.add(c2); hosType = new JTextField(); Theme.styleField(hosType); card.add(hosType);

            JLabel c3 = new JLabel("Hostel Fee (per year):"); Theme.styleLabel(c3);
            card.add(c3); hosFee = new JTextField(); Theme.styleField(hosFee); card.add(hosFee);

            JButton btnHos = new JButton("Add Hostel");
            Theme.styleButton(btnHos, Theme.ACCENT, Theme.ACCENT_DARK);
            card.add(btnHos); card.add(new JLabel(""));

            JLabel d1 = new JLabel("--- Add Placement ---"); Theme.styleLabel(d1);
            card.add(d1); card.add(new JLabel(""));

            JLabel d2 = new JLabel("Student Name:"); Theme.styleLabel(d2);
            card.add(d2); studName = new JTextField(); Theme.styleField(studName); card.add(studName);

            JLabel d3 = new JLabel("Company:"); Theme.styleLabel(d3);
            card.add(d3); comp = new JTextField(); Theme.styleField(comp); card.add(comp);

            JLabel d4 = new JLabel("Package (LPA):"); Theme.styleLabel(d4);
            card.add(d4); pkg = new JTextField(); Theme.styleField(pkg); card.add(pkg);

            JLabel d5 = new JLabel("Student Image Path:"); Theme.styleLabel(d5);
            card.add(d5); studImg = new JTextField(); Theme.styleField(studImg); card.add(studImg);

            JButton btnPlace = new JButton("Add Placement");
            Theme.styleButton(btnPlace, Theme.WARN, Theme.WARN_DARK);
            card.add(btnPlace); card.add(new JLabel(""));

            add(card, BorderLayout.CENTER);

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
            if (facility.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Facility Name."); return; }
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement("INSERT INTO facilities (college_id, facility_name) VALUES (?, ?)");
                ps.setInt(1, cid);
                ps.setString(2, facility);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Facility Added!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        void addHostel() {
            int cid = getCollegeIdOrWarn();
            if (cid == -1) return;
            String type = hosType.getText().trim();
            String feeStr = hosFee.getText().trim();
            if (type.isEmpty() || feeStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Hostel Type and Fee."); return; }
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement("INSERT INTO hostels (college_id, type, fee) VALUES (?, ?, ?)");
                ps.setInt(1, cid);
                ps.setString(2, type);
                ps.setDouble(3, Double.parseDouble(feeStr));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Hostel Added!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        void addPlacement() {
            int cid = getCollegeIdOrWarn();
            if (cid == -1) return;
            String student = studName.getText().trim();
            String company = comp.getText().trim();
            String packStr = pkg.getText().trim();
            String imgPath = cleanPath(studImg.getText());
            if (student.isEmpty() || company.isEmpty() || packStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Fill Student, Company and Package."); return; }
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement("INSERT INTO placements (college_id, student_name, company, package_lpa, student_image_path) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, cid);
                ps.setString(2, student);
                ps.setString(3, company);
                ps.setDouble(4, Double.parseDouble(packStr));
                ps.setString(5, imgPath);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Placement Added!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    // =========================================================================
    // ADMIN: MANAGE COLLEGES (FIXED UI LAYOUT)
    // =========================================================================
    static class ManageCollegesPanel extends JPanel {
        DefaultTableModel model;
        JTable table;
        JTextField txtId, txtName, txtState, txtCourse, txtLoc, txtFees, txtImg;
        JTextArea txtDesc;

        public ManageCollegesPanel() {
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            model = new DefaultTableModel(new String[]{"ID", "Name", "State", "Course", "Location", "Fees"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(model);
            Theme.styleTable(table);

            loadColleges();
            
            JScrollPane tableScroll = Theme.styleScroll(new JScrollPane(table));
            tableScroll.setPreferredSize(new Dimension(800, 250)); 

            JPanel formWrapper = new JPanel(new BorderLayout());
            formWrapper.setBackground(Theme.BG);

            JPanel form = new JPanel(new GridLayout(8, 2, 10, 12));
            Theme.styleCard(form);

            txtId = new JTextField(); Theme.styleField(txtId); txtId.setEditable(false); txtId.setBackground(Theme.BORDER); txtId.setForeground(Color.BLACK);
            txtName = new JTextField(); Theme.styleField(txtName);
            txtState = new JTextField(); Theme.styleField(txtState);
            txtCourse = new JTextField(); Theme.styleField(txtCourse);
            txtLoc = new JTextField(); Theme.styleField(txtLoc);
            txtFees = new JTextField(); Theme.styleField(txtFees);
            txtImg = new JTextField(); Theme.styleField(txtImg);
            txtDesc = new JTextArea(3, 20); txtDesc.setLineWrap(true); txtDesc.setWrapStyleWord(true); Theme.styleTextArea(txtDesc);

            JLabel f1 = new JLabel("College ID:"); Theme.styleLabel(f1);
            JLabel f2 = new JLabel("Name:"); Theme.styleLabel(f2);
            JLabel f3 = new JLabel("State:"); Theme.styleLabel(f3);
            JLabel f4 = new JLabel("Course:"); Theme.styleLabel(f4);
            JLabel f5 = new JLabel("Location:"); Theme.styleLabel(f5);
            JLabel f6 = new JLabel("Fees:"); Theme.styleLabel(f6);
            JLabel f7 = new JLabel("Image Path:"); Theme.styleLabel(f7);
            JLabel f8 = new JLabel("Description:"); Theme.styleLabel(f8);

            form.add(f1); form.add(txtId);
            form.add(f2); form.add(txtName);
            form.add(f3); form.add(txtState);
            form.add(f4); form.add(txtCourse);
            form.add(f5); form.add(txtLoc);
            form.add(f6); form.add(txtFees);
            form.add(f7); form.add(txtImg);
            form.add(f8); form.add(Theme.styleScroll(new JScrollPane(txtDesc)));

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setBackground(Theme.BG);

            JButton btnRefresh = new JButton("Refresh");
            JButton btnUpdate = new JButton("Update");
            JButton btnDelete = new JButton("Delete");

            Theme.styleButton(btnRefresh, Theme.SLATE, Theme.SLATE_DARK);
            Theme.styleButton(btnUpdate, Theme.PRIMARY, Theme.PRIMARY_DARK);
            Theme.styleButton(btnDelete, Theme.DANGER, Theme.DANGER_DARK);

            buttons.add(btnRefresh);
            buttons.add(btnUpdate);
            buttons.add(btnDelete);

            formWrapper.add(form, BorderLayout.NORTH); 
            formWrapper.add(buttons, BorderLayout.CENTER);

            JScrollPane formScroll = new JScrollPane(formWrapper);
            formScroll.setBorder(null);
            formScroll.getVerticalScrollBar().setUnitIncrement(16); 

            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(tableScroll);
            splitPane.setBottomComponent(formScroll);
            splitPane.setResizeWeight(0.5); 
            splitPane.setBorder(null);

            SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(250));

            add(splitPane, BorderLayout.CENTER);

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
                            rs.getInt("college_id"), rs.getString("name"), rs.getString("state"),
                            rs.getString("course"), rs.getString("location"), rs.getDouble("fees")
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }

        void loadCollegeToForm(int id) {
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement("SELECT * FROM colleges WHERE college_id=?");
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
            if (txtId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Select a college row first!"); return; }
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
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        void deleteCollege() {
            if (txtId.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Select a college row first!"); return; }
            int choice = JOptionPane.showConfirmDialog(
                    this, "Delete this college?\nThis may also delete related facilities/hostels/placements.",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) return;
            try (Connection con = getConnection()) {
                if (con == null) return;
                int collegeId = Integer.parseInt(txtId.getText().trim());

                PreparedStatement ps = con.prepareStatement("DELETE FROM facilities WHERE college_id=?"); ps.setInt(1, collegeId); ps.executeUpdate();
                ps = con.prepareStatement("DELETE FROM hostels WHERE college_id=?"); ps.setInt(1, collegeId); ps.executeUpdate();
                ps = con.prepareStatement("DELETE FROM placements WHERE college_id=?"); ps.setInt(1, collegeId); ps.executeUpdate();
                ps = con.prepareStatement("DELETE FROM colleges WHERE college_id=?"); ps.setInt(1, collegeId); ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Deleted Successfully!");
                txtId.setText(""); txtName.setText(""); txtState.setText(""); txtCourse.setText("");
                txtLoc.setText(""); txtFees.setText(""); txtImg.setText(""); txtDesc.setText("");
                loadColleges();
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    // =========================================================================
    // 6. COLLEGE DASHBOARD (Restricted to specific College ID)
    // =========================================================================
    static class CollegeDashboard extends JFrame {
        String user;
        int collegeId;
        String collegeName = "Your College";

        public CollegeDashboard(String user, int collegeId) {
            this.user = user;
            this.collegeId = collegeId;

            try (Connection con = getConnection()) {
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement("SELECT name FROM colleges WHERE college_id = ?");
                    ps.setInt(1, collegeId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) collegeName = rs.getString("name");
                }
            } catch (Exception ignored) {}

            setTitle("College Portal - " + collegeName);
            setSize(1150, 750);
            setDefaultCloseOperation(EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            Theme.applyFrame(this);

            JPanel rightMenu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
            rightMenu.setOpaque(false);
            
            JButton btnLogout = new JButton("Logout");
            Theme.styleButton(btnLogout, Theme.DANGER, Theme.DANGER_DARK);
            rightMenu.add(btnLogout);

            add(Theme.appBar(collegeName + " Portal", "Manage your profile, add placements, and view applications", rightMenu), BorderLayout.NORTH);

            JTabbedPane tabs = new JTabbedPane();
            Theme.styleTabs(tabs);

            tabs.addTab("Update Profile", new CollegeProfileEditorPanel(collegeId));
            tabs.addTab("Manage Facilities & Hostels", new CollegeManageDetailsPanel(collegeId));
            tabs.addTab("Add Placements", new CollegePlacementsPanel(collegeId));
            tabs.addTab("Student Applications", new CollegeApplicationsPanel(collegeName));

            add(tabs, BorderLayout.CENTER);

            btnLogout.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(this, "Do you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    new LoginFrame();
                }
            });

            setVisible(true);
        }
    }

    // =========================================================================
    // COLLEGE DASHBOARD PANELS
    // =========================================================================
    /* 
        static class CollegeProfileEditorPanel extends JPanel {
    int myCollegeId;
    JTextField txtName, txtState, txtCourse, txtLoc, txtFees, txtImg;
    JTextArea txtDesc;

    public CollegeProfileEditorPanel(int collegeId) {
        this.myCollegeId = collegeId;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);

        // 1. Create the Form Panel
        JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
        Theme.styleCard(form);

        txtName = new JTextField(); Theme.styleField(txtName);
        txtState = new JTextField(); Theme.styleField(txtState);
        txtCourse = new JTextField(); Theme.styleField(txtCourse);
        txtLoc = new JTextField(); Theme.styleField(txtLoc);
        txtFees = new JTextField(); Theme.styleField(txtFees);
        txtImg = new JTextField(); Theme.styleField(txtImg);
        txtDesc = new JTextArea(4, 20); 
        txtDesc.setLineWrap(true); 
        txtDesc.setWrapStyleWord(true); 
        Theme.styleTextArea(txtDesc);

        form.add(new JLabel("Name:")); form.add(txtName);
        form.add(new JLabel("State:")); form.add(txtState);
        form.add(new JLabel("Course Focus:")); form.add(txtCourse);
        form.add(new JLabel("Location:")); form.add(txtLoc);
        form.add(new JLabel("Fees:")); form.add(txtFees);
        form.add(new JLabel("Image Path:")); form.add(txtImg);
        form.add(new JLabel("Description:")); form.add(Theme.styleScroll(new JScrollPane(txtDesc)));

        // 2. Create the Button Panel (Bottom)
        JButton btnUpdate = new JButton("Update My Details");
        Theme.styleButton(btnUpdate, Theme.PRIMARY, Theme.PRIMARY_DARK);
        
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Theme.BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10)); // Added padding
        bottom.add(btnUpdate);

        // 3. IMPORTANT: Changed layout positions
        // Use CENTER for the form so it fills available space
        add(new JScrollPane(form), BorderLayout.CENTER); 
        add(bottom, BorderLayout.SOUTH);

        loadMyData();
        btnUpdate.addActionListener(e -> updateMyData());
    }
    
    // ... loadMyData and updateMyData methods remain the same
}
*/

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 1. Profile Editor

    static class CollegeProfileEditorPanel extends JPanel {
        int myCollegeId;
        JTextField txtName, txtState, txtCourse, txtLoc, txtFees, txtImg;
        JTextArea txtDesc;

        public CollegeProfileEditorPanel(int collegeId) {
            this.myCollegeId = collegeId;
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            JPanel form = new JPanel(new GridLayout(7, 2, 10, 12));
            Theme.styleCard(form);

            txtName = new JTextField(); Theme.styleField(txtName);
            txtState = new JTextField(); Theme.styleField(txtState);
            txtCourse = new JTextField(); Theme.styleField(txtCourse);
            txtLoc = new JTextField(); Theme.styleField(txtLoc);
            txtFees = new JTextField(); Theme.styleField(txtFees);
            txtImg = new JTextField(); Theme.styleField(txtImg);
            txtDesc = new JTextArea(4, 20); txtDesc.setLineWrap(true); txtDesc.setWrapStyleWord(true); Theme.styleTextArea(txtDesc);

            form.add(new JLabel("Name:")); form.add(txtName);
            form.add(new JLabel("State:")); form.add(txtState);
            form.add(new JLabel("Course Focus:")); form.add(txtCourse);
            form.add(new JLabel("Location:")); form.add(txtLoc);
            form.add(new JLabel("Fees:")); form.add(txtFees);
            form.add(new JLabel("Image Path:")); form.add(txtImg);
            form.add(new JLabel("Description:")); form.add(Theme.styleScroll(new JScrollPane(txtDesc)));

            JButton btnUpdate = new JButton("Update My Details");
            Theme.styleButton(btnUpdate, Theme.PRIMARY, Theme.PRIMARY_DARK);
            
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.setBackground(Theme.BG);
            bottom.add(btnUpdate);

            add(form, BorderLayout.NORTH);
            add(bottom, BorderLayout.SOUTH);
            
            add(new JScrollPane(form), BorderLayout.CENTER); 
        add(bottom, BorderLayout.SOUTH);
            loadMyData();
            btnUpdate.addActionListener(e -> updateMyData());
        }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        void loadMyData() {
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement("SELECT * FROM colleges WHERE college_id=?");
                ps.setInt(1, myCollegeId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    txtName.setText(rs.getString("name"));
                    txtState.setText(rs.getString("state"));
                    txtCourse.setText(rs.getString("course"));
                    txtLoc.setText(rs.getString("location"));
                    txtFees.setText(String.valueOf(rs.getDouble("fees")));
                    txtImg.setText(rs.getString("image_path"));
                    txtDesc.setText(rs.getString("description"));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void updateMyData() {
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
                ps.setInt(8, myCollegeId);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Profile Updated Successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // 2. Manage Facilities & Hostels
    static class CollegeManageDetailsPanel extends JPanel {
        int myCollegeId;
        JTextField fac, hosType, hosFee;

        public CollegeManageDetailsPanel(int collegeId) {
            this.myCollegeId = collegeId;
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            JPanel card = new JPanel(new GridLayout(6, 2, 10, 12));
            Theme.styleCard(card);

            JLabel b1 = new JLabel("--- Add Facility ---"); Theme.styleLabel(b1);
            card.add(b1); card.add(new JLabel(""));

            card.add(new JLabel("Facility Name:")); 
            fac = new JTextField(); Theme.styleField(fac); card.add(fac);

            JButton btnFac = new JButton("Add Facility");
            Theme.styleButton(btnFac, Theme.PRIMARY, Theme.PRIMARY_DARK);
            card.add(btnFac); card.add(new JLabel(""));

            JLabel c1 = new JLabel("--- Add Hostel ---"); Theme.styleLabel(c1);
            card.add(c1); card.add(new JLabel(""));

            card.add(new JLabel("Hostel Room Type:")); 
            hosType = new JTextField(); Theme.styleField(hosType); card.add(hosType);

            card.add(new JLabel("Hostel Fee (per year):")); 
            hosFee = new JTextField(); Theme.styleField(hosFee); card.add(hosFee);

            JButton btnHos = new JButton("Add Hostel");
            Theme.styleButton(btnHos, Theme.ACCENT, Theme.ACCENT_DARK);
            card.add(btnHos); card.add(new JLabel(""));

            add(card, BorderLayout.NORTH);

            btnFac.addActionListener(e -> addFacility());
            btnHos.addActionListener(e -> addHostel());
        }

        void addFacility() {
            String facility = fac.getText().trim();
            if (facility.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Facility Name."); return; }
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement("INSERT INTO facilities (college_id, facility_name) VALUES (?, ?)");
                ps.setInt(1, myCollegeId);
                ps.setString(2, facility);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Facility Added!");
                fac.setText("");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }

        void addHostel() {
            String type = hosType.getText().trim();
            String feeStr = hosFee.getText().trim();
            if (type.isEmpty() || feeStr.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter Hostel Type and Fee."); return; }
            try (Connection con = getConnection()) {
                if (con == null) return;
                PreparedStatement ps = con.prepareStatement("INSERT INTO hostels (college_id, type, fee) VALUES (?, ?, ?)");
                ps.setInt(1, myCollegeId);
                ps.setString(2, type);
                ps.setDouble(3, Double.parseDouble(feeStr));
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Hostel Added!");
                hosType.setText(""); hosFee.setText("");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
        }
    }

    // 3. Add Placements
    static class CollegePlacementsPanel extends JPanel {
        int myCollegeId;
        JTextField studName, comp, pkg, studImg;

        public CollegePlacementsPanel(int collegeId) {
            this.myCollegeId = collegeId;
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            JPanel card = new JPanel(new GridLayout(5, 2, 10, 12));
            Theme.styleCard(card);

            card.add(new JLabel("Student Name:")); 
            studName = new JTextField(); Theme.styleField(studName); card.add(studName);

            card.add(new JLabel("Company:")); 
            comp = new JTextField(); Theme.styleField(comp); card.add(comp);

            card.add(new JLabel("Package (LPA):")); 
            pkg = new JTextField(); Theme.styleField(pkg); card.add(pkg);

            card.add(new JLabel("Student Image Path:")); 
            studImg = new JTextField(); Theme.styleField(studImg); card.add(studImg);

            JButton btnPlace = new JButton("Add Placement");
            Theme.styleButton(btnPlace, Theme.WARN, Theme.WARN_DARK);
            
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.setBackground(Theme.BG);
            bottom.add(btnPlace);

            add(card, BorderLayout.NORTH);
            add(bottom, BorderLayout.CENTER);

            btnPlace.addActionListener(e -> addPlacement());
        }

        void addPlacement() {
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
                ps.setInt(1, myCollegeId);
                ps.setString(2, student);
                ps.setString(3, company);
                ps.setDouble(4, Double.parseDouble(packStr));
                ps.setString(5, imgPath);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Placement Added!");
                studName.setText(""); comp.setText(""); pkg.setText(""); studImg.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // 4. View Applications (With Email and Phone)
    static class CollegeApplicationsPanel extends JPanel {
        DefaultTableModel model;
        JTable table;
        String myCollegeName;

        public CollegeApplicationsPanel(String collegeName) {
            this.myCollegeName = collegeName;
            setLayout(new BorderLayout(10, 10));
            setBackground(Theme.BG);

            model = new DefaultTableModel(new String[]{"App ID", "Student Username", "Email", "Phone", "Status"}, 0) {
                @Override public boolean isCellEditable(int row, int column) { return false; }
            };
            table = new JTable(model);
            Theme.styleTable(table);

            loadApplications();

            JButton btnRefresh = new JButton("Refresh Applications");
            Theme.styleButton(btnRefresh, Theme.PRIMARY, Theme.PRIMARY_DARK);
            btnRefresh.addActionListener(e -> loadApplications());

            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setBackground(Theme.BG);
            topPanel.add(btnRefresh);

            add(topPanel, BorderLayout.NORTH);
            add(Theme.styleScroll(new JScrollPane(table)), BorderLayout.CENTER);
        }

        void loadApplications() {
            model.setRowCount(0);
            try (Connection con = getConnection()) {
                if (con == null) return;
                
                PreparedStatement ps = con.prepareStatement(
                        "SELECT a.id, a.student_name, u.email, u.phone " +
                        "FROM applications a " +
                        "LEFT JOIN users u ON a.student_name = u.username " +
                        "WHERE a.college_name = ? ORDER BY a.id DESC"
                );
                ps.setString(1, myCollegeName);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String email = rs.getString("email");
                    String phone = rs.getString("phone");
                    
                    model.addRow(new Object[]{
                        rs.getInt("id"), 
                        rs.getString("student_name"), 
                        (email != null && !email.isEmpty()) ? email : "Not Provided", 
                        (phone != null && !phone.isEmpty()) ? phone : "Not Provided", 
                        "Pending"
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}