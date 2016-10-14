package adb.wifi.woaiwhz.window;

import adb.wifi.woaiwhz.base.Config;
import adb.wifi.woaiwhz.component.Notify;
import adb.wifi.woaiwhz.listener.CustomInputVerifier;
import adb.wifi.woaiwhz.listener.LaunchWebBrowser;
import adb.wifi.woaiwhz.listener.NumberDocumentFilter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by huazhou.whz on 2016/10/7.
 */
public class RootWindowHolder implements ToolWindowFactory,ActionListener{
    private JPanel mRoot;
    private JTextField mIP_1;
    private JTextField mIP_2;
    private JTextField mIP_3;
    private JTextField mIP_4;
    private JTextField mPort;
    private JTextField[] mIPTexts;
    private JButton mConnectButton;
    private JList mConnectedDevices;
    private JPanel mEmptyView;
    private JPanel mDownContainer;
    private JLabel mGetHelp;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        attach2ToolWindow(toolWindow);
        attach2Project(project);
        init();
    }

    private void attach2ToolWindow(ToolWindow toolWindow){
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(mRoot, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private void attach2Project(Project project){
        // TODO: 2016/10/7
    }

    private void init(){
        initIpText();
        initPortText();
        initConnectButton();
        initOthersComponent();
        listenOthersKeyInput();
        mDownContainer.remove(mEmptyView);
    }

    private void initConnectButton(){
        mConnectButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mConnectButton.addActionListener(this);
    }

    private void initPortText(){
        final NumberDocumentFilter documentFilter = new NumberDocumentFilter(5);
        final InputVerifier verifier = new CustomInputVerifier(1 << 16,5555);

        documentFilter.bind(mPort);
        mPort.setInputVerifier(verifier);

        mPort.setText(String.valueOf(Config.DEFAULT_PORT));
    }

    private void initIpText(){
        mIPTexts = new JTextField[]{
                mIP_1,mIP_2,mIP_3,mIP_4
        };

        for (JTextField item : mIPTexts){
            final NumberDocumentFilter documentFilter = new NumberDocumentFilter(3);
            final InputVerifier verifier = new CustomInputVerifier(1 << 8);

            documentFilter.bind(item);
            item.setInputVerifier(verifier);
        }
    }

    private void listenOthersKeyInput(){
        final JTextField[] textFields = new JTextField[]{
                mIP_1,mIP_2,mIP_3,mIP_4,mPort
        };

        final KeyListener enterKeyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                final int key = e.getKeyChar();

                if(key == KeyEvent.VK_ENTER){
                    handleConnection();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        };

        final KeyListener autoFocusNext = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                final int key = e.getKeyChar();

                if(key == KeyEvent.VK_PERIOD){
                    KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        };

        for (JTextField field : textFields){
            field.addKeyListener(enterKeyListener);

            if(field == mIP_4 || field == mPort){
                return;
            }

            field.addKeyListener(autoFocusNext);
        }
    }

    private void initOthersComponent() {
        mGetHelp.addMouseListener(new LaunchWebBrowser(Config.HELP));
        mGetHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Object source = e.getSource();

        if(source == mConnectButton){
            handleConnection();
        }
    }

    private void handleConnection(){
        if(!verifyIpText() || !verifyPortText()){
            Notify.error("Fail to verify ip address or port number");
            return;
        }

        Notify.alert("success : " + gainIpAddressWithPortNumber());
        cleanText();
    }

    private String gainIpAddressWithPortNumber(){
        final StringBuilder builder = new StringBuilder();

        for (JTextField field : mIPTexts){
            builder.append(field.getText());
            builder.append(".");
        }

        final int length = builder.length();
        builder.replace(length - 1,length,":");
        builder.append(mPort.getText());

        return builder.toString();
    }

    private boolean verifyPortText(){
        final String text = mPort.getText();

        if(TextUtils.isBlank(text)){
            return false;
        }

        try {
            int port = Integer.valueOf(text);

            if(port >= 0 && port < 1 << 16){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();

        }

        return false;
    }

    private void cleanText(){
        for(JTextField field : mIPTexts){
            field.setText("");
        }
        mPort.setText(String.valueOf(Config.DEFAULT_PORT));
    }

    private boolean verifyIpText(){
        for (JTextField field : mIPTexts){
            final String text = field.getText();

            if(TextUtils.isBlank(text)){
                return false;
            }

            try{
                int ip = Integer.valueOf(text);
                if(ip < 0 && ip >= 1 << 8){
                    return false;
                }
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }
}