const app = getApp();

function getUserAvatar() {
  var user = app.globalData.userInfo;
  return user ? (user.avatarUrl || "") : "";
}

Page({
  data: {
    view: "list",
    conversations: [],
    myAvatar: "",
    loading: false,

    // 聊天详情
    chatOrderId: null,
    chatPeerName: "",
    chatPeerAvatar: "",
    chatMessages: [],
    inputText: "",
    scrollToView: ""
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 2 });
    }
    this.setData({ myAvatar: getUserAvatar() });
    // 列表视图显示 tabBar，聊天视图隐藏 tabBar
    if (this.data.view === "list") {
      if (typeof this.getTabBar === 'function' && this.getTabBar()) {
        this.getTabBar().setData({ show: true });
      }
      this.loadConversations();
    } else {
      this.loadMessages(this.data.chatOrderId);
    }
  },

  // ==================== 会话列表 ====================

  loadConversations() {
    var that = this;
    that.setData({ loading: true });
    app.get("/api/chat/conversations").then(function (data) {
      var list = (data || []).map(function (c) {
        return {
          orderId: c.orderId,
          orderNo: c.orderNo,
          orderType: c.orderType,
          orderStatus: c.orderStatus,
          peerName: c.peerName,
          peerAvatar: c.peerAvatar || "",
          lastMsg: c.lastMsg,
          lastTime: c.lastTime,
          unread: c.unread || 0,
          hasUnread: (c.unread || 0) > 0
        };
      });
      that.setData({ conversations: list, loading: false });
    }).catch(function (err) {
      console.error("加载会话失败", err);
      that.setData({ loading: false });
    });
  },

  openChat(event) {
    var orderId = event.currentTarget.dataset.orderId;
    var peerName = event.currentTarget.dataset.peerName;
    var peerAvatar = event.currentTarget.dataset.peerAvatar;
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ show: false });
    }
    this.setData({
      view: "chat",
      chatOrderId: orderId,
      chatPeerName: peerName,
      chatPeerAvatar: peerAvatar || ""
    });
    this.loadMessages(orderId);
  },

  // ==================== 聊天详情 ====================

  loadMessages(orderId) {
    var that = this;
    app.get("/api/chat/" + orderId).then(function (data) {
      if (!data || data.error) return;
      var msgs = (data.messages || []).map(function (m) {
        return {
          id: m.id,
          content: m.content,
          isMe: m.isMe,
          time: m.time
        };
      });
      that.setData({
        chatMessages: msgs,
        chatPeerName: data.peerName || that.data.chatPeerName,
        chatPeerAvatar: data.peerAvatar || that.data.chatPeerAvatar,
        scrollToView: msgs.length > 0 ? "msg-" + msgs[msgs.length - 1].id : ""
      });
    }).catch(function (err) {
      console.error("加载消息失败", err);
    });
  },

  onInput(event) {
    this.setData({ inputText: event.detail.value });
  },

  sendMessage() {
    var that = this;
    var text = that.data.inputText.trim();
    if (!text) return;

    that.setData({ inputText: "" });

    app.post("/api/chat/" + that.data.chatOrderId, { content: text }).then(function (msg) {
      if (!msg || msg.error) return;
      var msgs = that.data.chatMessages;
      msgs.push({
        id: msg.id,
        content: msg.content,
        isMe: true,
        time: "刚刚"
      });
      that.setData({
        chatMessages: msgs,
        scrollToView: "msg-" + msg.id
      });
    }).catch(function (err) {
      console.error("发送失败", err);
      wx.showToast({ title: "发送失败", icon: "none" });
    });
  },

  // ==================== 导航 ====================

  goBack() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ show: true });
    }
    this.setData({ view: "list", chatOrderId: null, chatMessages: [] });
    this.loadConversations();
  },

  goDetail(event) {
    var orderId = event.currentTarget.dataset.orderId;
    if (orderId) {
      wx.navigateTo({ url: "/pages/detail/detail?id=" + orderId });
    }
  }
});
