package quanlydethi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeIU extends JFrame {

    public HomeIU(){
        setTitle("Giao diện GridLayout");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setLayout(new GridLayout(2, 2, 15, 15));

        JButton btnTaoDeMoi = new JButton("Tạo đề mới");
        JButton btnNhapCauHoiHinhAnh = new JButton("Quét ảnh");
        JButton btnDanhSachDe = new JButton("Đề cũ");
        JButton btnNhapCauHoiThuCong = new JButton("Nhập thủ công");

    
        Dimension buttonSize = new Dimension(200, 80);
        btnTaoDeMoi.setPreferredSize(buttonSize);
        btnNhapCauHoiHinhAnh.setPreferredSize(buttonSize);
        btnDanhSachDe.setPreferredSize(buttonSize);
        btnNhapCauHoiThuCong.setPreferredSize(buttonSize);

        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        btnTaoDeMoi.setFont(buttonFont);
        btnNhapCauHoiHinhAnh.setFont(buttonFont);
        btnDanhSachDe.setFont(buttonFont);
        btnNhapCauHoiThuCong.setFont(buttonFont);

        // ActionListener commonActionListener = new ActionListener() {
        //     @Override
        //     public void actionPerformed(ActionEvent e) {
        //         JOptionPane.showMessageDialog(HomeIU.this, "Bạn đã nhấn nút: " + ((JButton)e.getSource()).getText());
        //     }
        // };

        btnTaoDeMoi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TaoDeNgauNhienFrame NewForm = new TaoDeNgauNhienFrame();
                NewForm.setVisible(true);
                dispose(); 
            }
        });

        btnNhapCauHoiHinhAnh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               ImageToTextFrame NewForm =  new ImageToTextFrame();
                NewForm.setVisible(true);
                dispose(); 
            }
        });
        btnDanhSachDe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
             DanhSachDeThiFrame NewForm = new DanhSachDeThiFrame();
             NewForm.setVisible(true);
             dispose(); 
            }
        });
        btnNhapCauHoiThuCong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               // new ManualInputFrame();
            }
        });

       
        add(wrapButton(btnTaoDeMoi));
        add(wrapButton(btnNhapCauHoiHinhAnh));
        add(wrapButton(btnDanhSachDe));
        add(wrapButton(btnNhapCauHoiThuCong));

        setLocationRelativeTo(null);
        setVisible(true);
    }

   
    private JPanel wrapButton(JButton button) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(button);
        panel.setOpaque(false);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new HomeIU();
            }
        });
    }
}

