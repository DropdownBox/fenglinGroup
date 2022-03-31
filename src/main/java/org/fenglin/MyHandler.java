package org.fenglin;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.MemberJoinEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.RichMessage;
import net.mamoe.mirai.message.data.SingleMessage;
import org.fenglin.Date.MyVerification;
import org.fenglin.config.ToolTipsConfig;
import org.fenglin.utils.TimerUtil;

import java.util.*;


public class MyHandler extends SimpleListenerHost {
    private final TimerUtil timerUtil = new TimerUtil();
    HashMap<String, Integer> kids = new HashMap<>();
    List bannedwords = Arrays.asList("习近平", "台湾", "恶俗", "esu", "180", "134", "153", "181", "189", "151");

    @EventHandler
    public ListeningStatus MemberJoinListener(MemberJoinEvent.Active event) {
        String name = event.getMember().getNick();
        //群验证
        if (ToolTipsConfig.ToolTipsConfig.getIsVerification()) {
            Random random = new Random();
            int a = random.nextInt(10);
            int b = random.nextInt(10);
            Map<Long, Integer> data = MyVerification.MyVerification.getInfo();
            data.put(event.getUser().getId(),a+b);
            MyVerification.MyVerification.setInfo(data);
            event.getUser().mute(30*24*60*60);
            String cod = a+"+"+b+"="+"?";
            event.getGroup().sendMessage(ToolTipsConfig.ToolTipsConfig.getVerification().replaceAll("%cod%",cod));
        }

        //群聊发送
        if (ToolTipsConfig.ToolTipsConfig.getIsMemberJoinGroup()) {
            event.getGroup().sendMessage(ToolTipsConfig.ToolTipsConfig.getMemberJoinGroup().replaceAll("%name%",name));
        }
        //私聊发送
        if (ToolTipsConfig.ToolTipsConfig.getIsMemberJoinPrivate()) {
            event.getMember().sendMessage(ToolTipsConfig.ToolTipsConfig.getMemberJoinPrivate().replaceAll("%name%",name));
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    public ListeningStatus MemberLeaveQuitListener(MemberLeaveEvent.Quit event) {
        String name = event.getMember().getNick();
        //群聊发送
        if (ToolTipsConfig.ToolTipsConfig.getIsMemberLeave()) {
            event.getGroup().sendMessage(ToolTipsConfig.ToolTipsConfig.getMemberLeave().replaceAll("%name%",name));
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    public ListeningStatus MemberLeaveKickListener(MemberLeaveEvent.Kick event) {
        String name = event.getMember().getNick();
        //群聊发送
        if (ToolTipsConfig.ToolTipsConfig.getIsKickMeber()) {
            event.getGroup().sendMessage(ToolTipsConfig.ToolTipsConfig.getKickMeber().replaceAll("%name%",name));
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    public ListeningStatus MessageEvent(MessageEvent event) {
        List<SingleMessage> usermsg = event.getMessage();
        String user = event.getSenderName();
        //禁止连续发送三张图片
        if (ToolTipsConfig.ToolTipsConfig.getIsPhotoLimited()) {
            if ((event.getMessage() instanceof Image || event.getMessage() instanceof RichMessage)){
                if(kids.containsKey(user)){
                    int i = kids.get(user);
                    kids.replace(user, i + 1);
                } else {
                    kids.put(user,0);
                }
            }
        }
        if (ToolTipsConfig.ToolTipsConfig.getIsMessagingContentLimited()){
            if(usermsg.contains(bannedwords)){
                mute((Member) event.getSender(),3600);
            }
        }
        return ListeningStatus.LISTENING;
    }

    @EventHandler
    public ListeningStatus Event(Event event){
        if(timerUtil.hasReached(5000)){
            kids.clear();
            timerUtil.reset();
        }
        return ListeningStatus.LISTENING;
    }

    private void mute(Member user, int i){
        user.mute(i);
    }
}
