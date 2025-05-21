package quanlydethi.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeIU extends JFrame {

    public HomeIU() {
        setTitle("Trang Chủ - Quản Lý Đề Thi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(550, 450);
        setLayout(new GridLayout(3, 2, 15, 15));

        JButton btnTaoDeMoi = new JButton("Tạo Đề Mới (Ngẫu Nhiên)");
        JButton btnNhapCauHoiHinhAnh = new JButton("Trích Xuất Câu Hỏi Từ Ảnh");
        JButton btnDanhSachDe = new JButton("Xem Danh Sách Đề Thi");
        JButton btnNhapCauHoiThuCong = new JButton("Nhập Câu Hỏi Thủ Công");
        JButton btnQuanLyCauHoi = new JButton("Quản Lý Ngân Hàng Câu Hỏi");

        Dimension buttonSize = new Dimension(200, 80);
        btnTaoDeMoi.setPreferredSize(buttonSize);
        btnNhapCauHoiHinhAnh.setPreferredSize(buttonSize);
        btnDanhSachDe.setPreferredSize(buttonSize);
        btnNhapCauHoiThuCong.setPreferredSize(buttonSize);
        btnQuanLyCauHoi.setPreferredSize(buttonSize);

        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        btnTaoDeMoi.setFont(buttonFont);
        btnNhapCauHoiHinhAnh.setFont(buttonFont);
        btnDanhSachDe.setFont(buttonFont);
        btnNhapCauHoiThuCong.setFont(buttonFont);
        btnQuanLyCauHoi.setFont(buttonFont);

        btnTaoDeMoi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TaoDeNgauNhienFrame taoDeNgauNhienFrame = new TaoDeNgauNhienFrame();
                taoDeNgauNhienFrame.setVisible(true);
                HomeIU.this.dispose();
            }
        });

        btnNhapCauHoiHinhAnh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageToTextFrame imageToTextFrame = new ImageToTextFrame();
                imageToTextFrame.setVisible(true);
                HomeIU.this.dispose();
            }
        });

        btnDanhSachDe.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DanhSachDeThiFrame danhSachDeThiFrame = new DanhSachDeThiFrame();
                danhSachDeThiFrame.setVisible(true);
                HomeIU.this.dispose();
            }
        });

        btnNhapCauHoiThuCong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NhapCauHoiFrame nhapCauHoiFrame = new NhapCauHoiFrame();
                nhapCauHoiFrame.setVisible(true);
                HomeIU.this.dispose();
            }
        });

        btnQuanLyCauHoi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                QuanLyNganHangCauHoiFrame qlCauHoiFrame = new QuanLyNganHangCauHoiFrame();
                qlCauHoiFrame.setVisible(true);
                HomeIU.this.dispose();
            }
        });

        add(wrapButton(btnTaoDeMoi));
        add(wrapButton(btnNhapCauHoiHinhAnh));
        add(wrapButton(btnDanhSachDe));
        add(wrapButton(btnNhapCauHoiThuCong));
        add(wrapButton(btnQuanLyCauHoi));

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel wrapButton(JButton button) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(button);
        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new HomeIU();
            }
        });
    }
}
