package mindustry.ui.fragments;

import arc.*;
import arc.Input.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.client.Client;
import mindustry.client.utils.Autocomplete;
import mindustry.client.utils.Autocompleteable;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.ui.*;

import java.util.Arrays;

import static arc.Core.*;
import static mindustry.Vars.net;
import static mindustry.Vars.*;

public class ChatFragment extends Table{
    private static final int messagesShown = 10;
    public Seq<ChatMessage> messages = new Seq<>();
    private float fadetime;
    private boolean shown = false;
    private TextField chatfield;
    private Label fieldlabel = new Label(">");
    private Font font;
    private GlyphLayout layout = new GlyphLayout();
    private float offsetx = Scl.scl(4), offsety = Scl.scl(4), fontoffsetx = Scl.scl(2), chatspace = Scl.scl(50);
    private Color shadowColor = new Color(0, 0, 0, 0.4f);
    private float textspacing = Scl.scl(10);
    private Seq<String> history = new Seq<>();
    private int historyPos = 0;
    private int scrollPos = 0;
    private Fragment container = new Fragment(){
        @Override
        public void build(Group parent){
            scene.add(ChatFragment.this);
        }
    };
    private Seq<Autocompleteable> completion = new Seq<>();
    private int completionPos = -1;

    public ChatFragment(){
        super();

        setFillParent(true);
        font = Fonts.def;

        visible(() -> {
            if(!net.active() && messages.size > 0){
                clearMessages();

                if(shown){
                    hide();
                }
            }

            return net.active() && ui.hudfrag.shown;
        });

        update(() -> {

            if(net.active() && input.keyTap(Binding.chat) && (scene.getKeyboardFocus() == chatfield || scene.getKeyboardFocus() == null || ui.minimapfrag.shown()) && !ui.scriptfrag.shown()){
                toggle();
            }

            if(shown){
                if(input.keyTap(Binding.chat_history_prev)){
//                    if(historyPos == 0) history.set(0, chatfield.getText());
//                    historyPos++;
//                    updateChat();
                    completionPos--;
                    completionPos = Math.max(completionPos, 0);
                    completionPos = Math.min(completionPos, completion.size);
                }
                if(input.keyTap(Binding.chat_history_next)){
//                    historyPos--;
//                    updateChat();
                    completionPos++;
                    completionPos = Math.max(completionPos, 0);
                    completionPos = Math.min(completionPos, completion.size);
                }
                if (input.keyTap(Binding.chat_autocomplete) && completion.any()) {
                    completionPos = Math.max(completionPos, 0);
                    completionPos = Math.min(completionPos, completion.size);

                    chatfield.setText(completion.get(completionPos).getCompletion(chatfield.getText()));
                    chatfield.setCursorPosition(chatfield.getText().length());
                }
                scrollPos = (int)Mathf.clamp(scrollPos + input.axis(Binding.chat_scroll), 0, Math.max(0, messages.size - messagesShown));
                if (Autocomplete.matches(chatfield.getText())) {
                    Seq<Autocompleteable> oldCompletion = completion.copy();
                    completion = Autocomplete.closest(chatfield.getText()).filter(item -> item.matches(chatfield.getText()) > 0.5f);
                    completion.reverse();
                    completion.truncate(4);
                    completion.reverse();
                    if (!Arrays.equals(completion.items, oldCompletion.items)) {
                        completionPos = completion.size - 1;
                    }
                }
            }
        });

        history.insert(0, "");
        setup();
    }

    public Fragment container(){
        return container;
    }

    public void clearMessages(){
        messages.clear();
        history.clear();
        history.insert(0, "");
    }

    private void setup(){
        fieldlabel.setStyle(new LabelStyle(fieldlabel.getStyle()));
        fieldlabel.getStyle().font = font;
        fieldlabel.setStyle(fieldlabel.getStyle());

        chatfield = new TextField("", new TextField.TextFieldStyle(scene.getStyle(TextField.TextFieldStyle.class)));
        chatfield.setMaxLength(Vars.maxTextLength);
        chatfield.getStyle().background = null;
        chatfield.getStyle().font = Fonts.chat;
        chatfield.getStyle().fontColor = Color.white;
        chatfield.setStyle(chatfield.getStyle());

        bottom().left().marginBottom(offsety).marginLeft(offsetx * 2).add(fieldlabel).padBottom(6f);

        add(chatfield).padBottom(offsety).padLeft(offsetx).growX().padRight(offsetx).height(28);

        if(Vars.mobile){
            marginBottom(105f);
            marginRight(240f);
        }
    }

    @Override
    public void draw(){
        float opacity = Core.settings.getInt("chatopacity") / 100f;
        float textWidth = Math.min(Core.graphics.getWidth()/1.5f, Scl.scl(700f));

        Draw.color(shadowColor);

        if(shown){
            Fill.crect(offsetx, chatfield.y, chatfield.getWidth() + 15f, chatfield.getHeight() - 1);
        }

        super.draw();

        float spacing = chatspace;

        chatfield.visible = shown;
        fieldlabel.visible = shown;

        Draw.color(shadowColor);
        Draw.alpha(shadowColor.a * opacity);

        float theight = offsety + spacing + getMarginBottom();
        for(int i = scrollPos; i < messages.size && i < messagesShown + scrollPos && (i < fadetime || shown); i++){

            layout.setText(font, messages.get(i).formattedMessage, Color.white, textWidth, Align.bottomLeft, true);
            theight += layout.height + textspacing;
            if(i - scrollPos == 0) theight -= textspacing + 1;

            font.getCache().clear();
            font.getCache().addText(messages.get(i).formattedMessage, fontoffsetx + offsetx, offsety + theight, textWidth, Align.bottomLeft, true);

            Color color = messages.get(i).backgroundColor;
            if (color == null) {
                color = shadowColor;
            }
            color.a = shadowColor.a;

            if(!shown && fadetime - i < 1f && fadetime - i >= 0f){
                font.getCache().setAlphas((fadetime - i) * opacity);
                Draw.color(color.r, color.g, color.b, shadowColor.a * (fadetime - i) * opacity);
            }else{
                font.getCache().setAlphas(opacity);
                Draw.color(color);
            }

            Fill.crect(offsetx, theight - layout.height - 2, textWidth + Scl.scl(4f), layout.height + textspacing);
            Draw.color(shadowColor);
            Draw.alpha(opacity * shadowColor.a);

            font.getCache().draw();
        }


        if(fadetime > 0 && !shown){
            fadetime -= Time.delta / 180f;
        }

        if (completion.size > 0 && shown) {
            float pos = Reflect.<FloatSeq>get(chatfield, "glyphPositions").peek();
            StringBuilder contents = new StringBuilder();
            int index = 0;
            for (Autocompleteable auto : completion) {
                String completion = auto.getHover(chatfield.getText());
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Math.min(completion.length(), chatfield.getText().length()); i++) {
                    if (completion.charAt(i) == chatfield.getText().charAt(i)) {
                        sb.append(completion.charAt(i));
                    } else {
                        break;
                    }
                }
                String ending = completion.substring(sb.length());
                if (index == completionPos) {
                    contents.append("[#a9d8ff]");
                }
                contents.append(ending);
                contents.append("[]\n");
                index++;
            }
            font.getCache().clear();
//            float height = font.getCache().getLayouts().sumf(item -> item.height);
            float height = font.getData().lineHeight * completion.size;
//            System.out.println(height);
            font.getCache().addText(contents.toString(), pos + offsetx + 17f, 10f + height);
            Draw.color(shadowColor);
            Fill.crect(pos + offsetx + 17f, 10f + font.getData().lineHeight, font.getCache().getLayouts().max(item -> item.width).width, height - font.getData().lineHeight);
            Draw.color();
            font.getCache().draw();
        }
        Draw.color();
    }

    private void sendMessage(){
        String message = chatfield.getText().trim();
        clearChatInput();

        if(message.isEmpty()) return;

        history.insert(1, message);

        //check if it's a command
        CommandHandler.CommandResponse response = Client.fooCommands.handleMessage(message, player);
        if(response.type == CommandHandler.ResponseType.noCommand){ //no command to handle


            Call.sendChatMessage(message);
            if (message.equals("/sync")) {
                Client.lastSyncTime = Time.millis();
            }

        }else{

            //a command was sent, now get the output
            if(response.type != CommandHandler.ResponseType.valid){
                String text;

                //send usage
                if(response.type == CommandHandler.ResponseType.manyArguments){
                    text = "[scarlet]Too many arguments. Usage:[lightgray] " + response.command.text + "[gray] " + response.command.paramText;
                }else if(response.type == CommandHandler.ResponseType.fewArguments){
                    text = "[scarlet]Too few arguments. Usage:[lightgray] " + response.command.text + "[gray] " + response.command.paramText;
                }else{ //unknown command
                    text = "[scarlet]Unknown command. Check [lightgray]!help[scarlet].";
                }

                player.sendMessage(text);
            }
        }
    }

    public void toggle(){

        if(!shown){
            scene.setKeyboardFocus(chatfield);
            shown = true;
            if(mobile){
                TextInput input = new TextInput();
                input.maxLength = maxTextLength;
                input.accepted = text -> {
                    chatfield.setText(text);
                    sendMessage();
                    hide();
                    Core.input.setOnscreenKeyboardVisible(false);
                };
                input.canceled = this::hide;
                Core.input.getTextInput(input);
            }else{
                chatfield.fireClick();
            }
        }else{
            //sending chat has a delay; workaround for issue #1943
            Time.run(2f, () ->{
                scene.setKeyboardFocus(null);
                shown = false;
                scrollPos = 0;
                sendMessage();
            });
        }
    }

    public void hide(){
        scene.setKeyboardFocus(null);
        shown = false;
        clearChatInput();
    }

    public void updateChat(){
        chatfield.setText(history.get(historyPos));
        chatfield.setCursorPosition(chatfield.getText().length());
    }

    public void clearChatInput(){
        historyPos = 0;
        history.set(0, "");
        chatfield.setText("");
    }

    public boolean shown(){
        return shown;
    }

    public ChatMessage addMessage(String message, String sender, Color background){
        if(sender == null && message == null) return null;
        ChatMessage msg = new ChatMessage(message, sender, background);
        messages.insert(0, msg);

        fadetime += 1f;
        fadetime = Math.min(fadetime, messagesShown) + 1f;
        
        if(scrollPos > 0) scrollPos++;
        return msg;
    }

    public ChatMessage addMessage(String message, String sender) {
        return addMessage(message, sender, null);
    }

    public static class ChatMessage{
        public final String sender;
        public String message;
        public String formattedMessage;
        public Color backgroundColor = null;

        public ChatMessage(String message, String sender){
            this.message = message;
            this.sender = sender;
            format();
        }

        public ChatMessage(String message, String sender, Color color){
            this.message = message;
            this.sender = sender;
            backgroundColor = color;
            format();
        }

        public void format() {
            if(sender == null){ //no sender, this is a server message?
                formattedMessage = message == null ? "" : message;
            }else {
                formattedMessage = "[coral][[" + sender + "[coral]]:[white] " + message;
            }
        }
    }

}
