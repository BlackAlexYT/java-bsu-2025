package com.bsu.transactions;

import com.bsu.transactions.async.TransactionEngine;
import com.bsu.transactions.model.Account;
import com.bsu.transactions.model.Transaction;
import com.bsu.transactions.model.TransactionType;
import com.bsu.transactions.model.User;
import com.bsu.transactions.patterns.observer.AccountSubject;
import com.bsu.transactions.repository.InMemoryAccountRepository;
import com.bsu.transactions.repository.InMemoryUserRepository;
import com.bsu.transactions.service.AccountService;
import com.bsu.transactions.service.TransactionService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Main {

    public static void main(String[] args) {
        InMemoryUserRepository userRepo = new InMemoryUserRepository();
        InMemoryAccountRepository accountRepo = new InMemoryAccountRepository();

        AccountService accountService = new AccountService(accountRepo);
        AccountSubject subject = new AccountSubject();
        TransactionService txService = new TransactionService(accountService, subject);
        TransactionEngine engine = new TransactionEngine(txService, accountService, Runtime.getRuntime().availableProcessors());

        User alice = new User(UUID.randomUUID(), "alice");
        UUID a1 = UUID.randomUUID();
        UUID a2 = UUID.randomUUID();
        alice.addAccountId(a1);
        alice.addAccountId(a2);
        userRepo.save(alice);
        accountRepo.save(new Account(a1, BigDecimal.valueOf(1000)));
        accountRepo.save(new Account(a2, BigDecimal.valueOf(500)));

        User bob = new User(UUID.randomUUID(), "bob");
        UUID b1 = UUID.randomUUID();
        UUID b2 = UUID.randomUUID();
        bob.addAccountId(b1);
        bob.addAccountId(b2);
        userRepo.save(bob);
        accountRepo.save(new Account(b1, BigDecimal.valueOf(300)));
        accountRepo.save(new Account(b2, BigDecimal.valueOf(150)));


        SwingUtilities.invokeLater(() -> createUI(userRepo, accountService, txService));

        Runtime.getRuntime().addShutdownHook(new Thread(engine::shutdown));
    }

    private static void refreshUI(JComboBox<User> userBox,
                                  JComboBox<User> targetUserBox,
                                  JComboBox<UUID> accountBox,
                                  JComboBox<UUID> targetAccountBox,
                                  InMemoryUserRepository userRepo) {

        User selectedSourceUser = (User) userBox.getSelectedItem();
        User selectedTargetUser = (User) targetUserBox.getSelectedItem();
        UUID selectedSourceAccount = (UUID) accountBox.getSelectedItem();
        UUID selectedTargetAccount = (UUID) targetAccountBox.getSelectedItem();

        DefaultComboBoxModel<User> sourceModel = (DefaultComboBoxModel<User>) userBox.getModel();
        DefaultComboBoxModel<User> targetModel = (DefaultComboBoxModel<User>) targetUserBox.getModel();

        ActionListener[] sourceListeners = userBox.getActionListeners();
        ActionListener[] targetListeners = targetUserBox.getActionListeners();
        for (ActionListener l : sourceListeners) userBox.removeActionListener(l);
        for (ActionListener l : targetListeners) targetUserBox.removeActionListener(l);

        sourceModel.removeAllElements();
        targetModel.removeAllElements();
        userRepo.findAll().forEach(user -> {
            sourceModel.addElement(user);
            targetModel.addElement(user);
        });

        if (selectedSourceUser != null && sourceModel.getIndexOf(selectedSourceUser) >= 0) {
            userBox.setSelectedItem(selectedSourceUser);
        } else if (sourceModel.getSize() > 0) {
            userBox.setSelectedItem(sourceModel.getElementAt(0));
        }

        if (selectedTargetUser != null && targetModel.getIndexOf(selectedTargetUser) >= 0) {
            targetUserBox.setSelectedItem(selectedTargetUser);
        } else if (targetModel.getSize() > 0) {
            targetUserBox.setSelectedItem(targetModel.getElementAt(0));
        }

        getItemsCheck(userBox, accountBox, selectedSourceAccount);

        getItemsCheck(targetUserBox, targetAccountBox, selectedTargetAccount);

        for (ActionListener l : sourceListeners) userBox.addActionListener(l);
        for (ActionListener l : targetListeners) targetUserBox.addActionListener(l);
    }

    private static void getItemsCheck(JComboBox<User> userBox, JComboBox<UUID> accountBox, UUID selectedSourceAccount) {
        User sourceUser = (User) userBox.getSelectedItem();
        if (sourceUser != null) {
            accountBox.removeAllItems();
            sourceUser.getAccountIds().forEach(accountBox::addItem);
            if (selectedSourceAccount != null && sourceUser.getAccountIds().contains(selectedSourceAccount)) {
                accountBox.setSelectedItem(selectedSourceAccount);
            } else if (!sourceUser.getAccountIds().isEmpty()) {
                accountBox.setSelectedItem(sourceUser.getAccountIds().get(0));
            }
        }
    }


    private static void createUI(InMemoryUserRepository userRepo, AccountService accountService, TransactionService txService) {


        JFrame frame = new JFrame("Bank Transactions Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1800, 1200);
        frame.setLayout(new BorderLayout());

        JLabel timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        frame.add(timeLabel, BorderLayout.NORTH);
        new Timer(1000, e -> timeLabel.setText("Current Time: " + java.time.LocalTime.now().withNano(0))).start();

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 20));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        logArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void scrollToBottom() {
                SwingUtilities.invokeLater(() -> logArea.setCaretPosition(logArea.getDocument().getLength()));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { scrollToBottom(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { scrollToBottom(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { scrollToBottom(); }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(18, 18, 18, 18);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Arial", Font.BOLD, 22);
        Font btnFont = new Font("Arial", Font.BOLD, 22);

        DefaultComboBoxModel<User> sourceUserModel = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<User> targetUserModel = new DefaultComboBoxModel<>();
        userRepo.findAll().forEach(sourceUserModel::addElement);
        userRepo.findAll().forEach(targetUserModel::addElement);

        JComboBox<User> userBox = new JComboBox<>(sourceUserModel);
        JComboBox<User> targetUserBox = new JComboBox<>(targetUserModel);

        ListCellRenderer<? super User> userRenderer = (list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            if (value == null) {
                lbl.setText("");
            } else {
                java.math.BigDecimal total = value.getAccountIds().stream()
                        .map(accId -> accountService.findById(accId)
                                .map(Account::getBalance)
                                .orElse(BigDecimal.ZERO))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                lbl.setText(String.format("%s — %d acc (total %s)", value.getNickname(), value.getAccountIds().size(), total));
            }
            lbl.setFont(new Font("Arial", Font.PLAIN, 20));
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            return lbl;
        };
        userBox.setRenderer(userRenderer);
        targetUserBox.setRenderer(userRenderer);
        userBox.setFont(new Font("Arial", Font.PLAIN, 20));
        targetUserBox.setFont(new Font("Arial", Font.PLAIN, 20));
        userBox.setPreferredSize(new Dimension(600, 48));
        targetUserBox.setPreferredSize(new Dimension(600, 48));

        JComboBox<java.util.UUID> accountBox = new JComboBox<>();
        JComboBox<java.util.UUID> targetAccountBox = new JComboBox<>();
        accountBox.setFont(new Font("Arial", Font.PLAIN, 22));
        targetAccountBox.setFont(new Font("Arial", Font.PLAIN, 22));
        accountBox.setPreferredSize(new Dimension(700, 48));
        targetAccountBox.setPreferredSize(new Dimension(700, 48));

        ListCellRenderer<? super java.util.UUID> accRenderer = (list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel();
            lbl.setOpaque(true);
            if (value == null) {
                lbl.setText("");
            } else {
                String shortId = value.toString().substring(0, 6) + "...";
                String bal = accountService.findById(value).map(a -> a.getBalance().toString()).orElse("N/A");
                lbl.setText(shortId + " — " + bal);
            }
            lbl.setFont(new Font("Arial", Font.PLAIN, 20));
            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } else {
                lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            return lbl;
        };
        accountBox.setRenderer(accRenderer);
        targetAccountBox.setRenderer(accRenderer);

        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(amountLabel, gbc);
        JTextField amountField = new JTextField();
        amountField.setFont(new Font("Arial", Font.PLAIN, 22));
        amountField.setPreferredSize(new Dimension(300, 50));
        gbc.gridx = 1;
        panel.add(amountField, gbc);

        JLabel userLabel = new JLabel("Source User:");
        userLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(userLabel, gbc);
        gbc.gridx = 1;
        panel.add(userBox, gbc);

        JLabel accountLabel = new JLabel("Source Account:");
        accountLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(accountLabel, gbc);
        gbc.gridx = 1;
        panel.add(accountBox, gbc);

        JLabel targetUserLabel = new JLabel("Target User:");
        targetUserLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(targetUserLabel, gbc);
        gbc.gridx = 1;
        panel.add(targetUserBox, gbc);

        JLabel targetAccountLabel = new JLabel("Target Account:");
        targetAccountLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(targetAccountLabel, gbc);
        gbc.gridx = 1;
        panel.add(targetAccountBox, gbc);

        java.util.function.Consumer<User> updateSourceAccounts = u -> {
            accountBox.removeAllItems();
            if (u == null) return;
            u.getAccountIds().forEach(accountBox::addItem);
        };
        java.util.function.Consumer<User> updateTargetAccounts = u -> {
            targetAccountBox.removeAllItems();
            if (u == null) return;
            u.getAccountIds().forEach(targetAccountBox::addItem);
        };

        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        depositBtn.setFont(btnFont); withdrawBtn.setFont(btnFont); transferBtn.setFont(btnFont);
        depositBtn.setPreferredSize(new Dimension(350, 70));
        withdrawBtn.setPreferredSize(new Dimension(350, 70));
        transferBtn.setPreferredSize(new Dimension(350, 70));

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(depositBtn, gbc);
        gbc.gridx = 1;
        panel.add(withdrawBtn, gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        panel.add(transferBtn, gbc);
        gbc.gridwidth = 1;

        JButton balanceBtn = new JButton("Show Balance");
        balanceBtn.setFont(btnFont);
        balanceBtn.setPreferredSize(new Dimension(300, 60));
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        panel.add(balanceBtn, gbc);
        gbc.gridwidth = 1;

        JLabel nicknameLabel = new JLabel("New nickname:");
        nicknameLabel.setFont(labelFont);
        gbc.gridx = 0; gbc.gridy = 8;
        panel.add(nicknameLabel, gbc);
        gbc.gridx = 1;
        JTextField newNameField = new JTextField();
        newNameField.setFont(new Font("Arial", Font.PLAIN, 20));
        newNameField.setPreferredSize(new Dimension(300, 45));
        panel.add(newNameField, gbc);

        JButton addUserBtn = new JButton("Add User");
        addUserBtn.setFont(btnFont);
        addUserBtn.setPreferredSize(new Dimension(200, 50));
        JButton renameBtn = new JButton("Rename User");
        renameBtn.setFont(btnFont);
        renameBtn.setPreferredSize(new Dimension(240, 50));
        gbc.gridx = 0; gbc.gridy = 9;
        panel.add(addUserBtn, gbc);
        gbc.gridx = 1;
        panel.add(renameBtn, gbc);

        if (sourceUserModel.getSize() > 0) {
            User first = sourceUserModel.getElementAt(0);
            userBox.setSelectedItem(first);
            updateSourceAccounts.accept(first);
        }
        if (targetUserModel.getSize() > 0) {
            User firstT = targetUserModel.getElementAt(0);
            targetUserBox.setSelectedItem(firstT);
            updateTargetAccounts.accept(firstT);
        }

        userBox.addActionListener(e -> updateSourceAccounts.accept((User) userBox.getSelectedItem()));
        targetUserBox.addActionListener(e -> updateTargetAccounts.accept((User) targetUserBox.getSelectedItem()));


        balanceBtn.addActionListener(e -> {
            try {
                java.util.UUID accId = (java.util.UUID) accountBox.getSelectedItem();
                if (accId == null) { logArea.append("No source account selected\n"); return; }
                accountService.findById(accId).ifPresentOrElse(
                        acc -> logArea.append("Balance for account " + accId + ": " + acc.getBalance() + "\n"),
                        () -> logArea.append("Account not found: " + accId + "\n")
                );
            } catch (Exception ex) {
                logArea.append("Error: " + ex.getMessage() + "\n");
            }
        });

        depositBtn.addActionListener(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) { logArea.append("Error: amount must be positive\n"); return; }
                java.util.UUID accountId = (java.util.UUID) accountBox.getSelectedItem();
                if (accountId == null) { logArea.append("No account selected\n"); return; }
                Transaction tx = new Transaction(java.util.UUID.randomUUID(), Instant.now(), TransactionType.DEPOSIT, amount, accountId, null);
                txService.processTransaction(tx);
                logArea.append(Instant.now() + "  Deposited " + amount + " to account " + accountId + "\n");

                refreshUI(userBox, targetUserBox, accountBox, targetAccountBox, userRepo);
            } catch (NumberFormatException nfe) {
                logArea.append("Error: invalid number\n");
            } catch (Exception ex) {
                logArea.append("Error: " + ex.getMessage() + "\n");
            }
        });

        withdrawBtn.addActionListener(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) { logArea.append("Error: amount must be positive\n"); return; }
                java.util.UUID accountId = (java.util.UUID) accountBox.getSelectedItem();
                if (accountId == null) { logArea.append("No account selected\n"); return; }
                Transaction tx = new Transaction(java.util.UUID.randomUUID(), Instant.now(), TransactionType.WITHDRAW, amount, accountId, null);
                txService.processTransaction(tx);
                logArea.append(Instant.now() + "  Withdrew " + amount + " from account " + accountId + "\n");

                refreshUI(userBox, targetUserBox, accountBox, targetAccountBox, userRepo);
            } catch (NumberFormatException nfe) {
                logArea.append("Error: invalid number\n");
            } catch (Exception ex) {
                logArea.append("Error: " + ex.getMessage() + "\n");
            }
        });

        transferBtn.addActionListener(e -> {
            try {
                BigDecimal amount = new BigDecimal(amountField.getText());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) { logArea.append("Error: amount must be positive\n"); return; }
                java.util.UUID source = (java.util.UUID) accountBox.getSelectedItem();
                java.util.UUID target = (java.util.UUID) targetAccountBox.getSelectedItem();
                if (source == null) { logArea.append("No source account selected\n"); return; }
                if (target == null) { logArea.append("No target account selected\n"); return; }
                Transaction tx = new Transaction(java.util.UUID.randomUUID(), Instant.now(), TransactionType.TRANSFER, amount, source, target);
                txService.processTransaction(tx);
                logArea.append(Instant.now() + "  Transferred " + amount + " from " + source + " to " + target + "\n");

                refreshUI(userBox, targetUserBox, accountBox, targetAccountBox, userRepo);
            } catch (NumberFormatException nfe) {
                logArea.append("Error: invalid number\n");
            } catch (Exception ex) {
                logArea.append("Error: " + ex.getMessage() + "\n");
            }
        });

        addUserBtn.addActionListener(e -> {
            try {
                String nick = newNameField.getText().trim();
                if (nick.isEmpty()) {
                    logArea.append("Error: nickname cannot be empty\n");
                    return;
                }

                User newUser = new User(UUID.randomUUID(), nick);
                UUID newAcc = UUID.randomUUID();
                newUser.addAccountId(newAcc);

                userRepo.save(newUser);
                accountService.save(new Account(newAcc, BigDecimal.ZERO));

                User selectedSourceUser = (User) userBox.getSelectedItem();
                User selectedTargetUser = (User) targetUserBox.getSelectedItem();
                UUID selectedSourceAcc = (UUID) accountBox.getSelectedItem();
                UUID selectedTargetAcc = (UUID) targetAccountBox.getSelectedItem();

                sourceUserModel.addElement(newUser);
                targetUserModel.addElement(newUser);

                userBox.setSelectedItem(selectedSourceUser != null ? selectedSourceUser : newUser);
                targetUserBox.setSelectedItem(selectedTargetUser != null ? selectedTargetUser : newUser);

                refreshUI(userBox, targetUserBox, accountBox, targetAccountBox, userRepo);

                logArea.append("Added user " + nick + " (" + newUser.getId() + "), account " + newAcc + "\n");
            } catch (Exception ex) {
                logArea.append("Error: " + ex.getMessage() + "\n");
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        JButton importBtn = new JButton("Import CSV");
        JButton exportBtn = new JButton("Export CSV");
        importBtn.setFont(new Font("Arial", Font.BOLD, 20));
        exportBtn.setFont(new Font("Arial", Font.BOLD, 20));
        bottomPanel.add(importBtn);
        bottomPanel.add(exportBtn);

        frame.add(bottomPanel, BorderLayout.SOUTH);

        importBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File file = chooser.getSelectedFile();
                    importFromCSV(file, userRepo, accountService);
                    refreshUI(userBox, targetUserBox, accountBox, targetAccountBox, userRepo);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error importing CSV: " + ex.getMessage());
                }
            }
        });

        exportBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showSaveDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File file = chooser.getSelectedFile();
                    exportToCSV(file, userRepo, accountService);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error exporting CSV: " + ex.getMessage());
                }
            }
        });

        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void importFromCSV(java.io.File file, InMemoryUserRepository userRepo, AccountService accountService) throws Exception {
        userRepo.clear();
        accountService.clear();

        java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            String[] parts = line.split(",", 3);
            UUID userId = UUID.fromString(parts[0]);
            String nickname = parts[1];
            User user = new User(userId, nickname);

            if (parts.length > 2 && !parts[2].isEmpty()) {
                String[] accs = parts[2].split(";");
                for (String acc : accs) {
                    if (acc.trim().isEmpty()) continue;
                    String[] accParts = acc.split(":");
                    UUID accId = UUID.fromString(accParts[0]);
                    BigDecimal balance = new BigDecimal(accParts[1]);
                    user.addAccountId(accId);
                    accountService.save(new Account(accId, balance));
                }
            }

            userRepo.save(user);
        }
    }

    private static void exportToCSV(java.io.File file, InMemoryUserRepository userRepo, AccountService accountService) throws Exception {
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (User user : userRepo.findAll()) {
            StringBuilder sb = new StringBuilder();
            sb.append(user.getId()).append(",").append(user.getNickname()).append(",");

            java.util.List<String> accStrings = new java.util.ArrayList<>();
            for (UUID accId : user.getAccountIds()) {
                accountService.findById(accId).ifPresent(acc -> accStrings.add(acc.getId() + ":" + acc.getBalance()));
            }
            sb.append(String.join(";", accStrings));
            lines.add(sb.toString());
        }
        java.nio.file.Files.write(file.toPath(), lines);
    }



}


