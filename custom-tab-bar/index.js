Component({
  data: {
    selected: 0,
    show: true,
    list: [
      { pagePath: "/pages/home/home", text: "下单", icon: "📦" },
      { pagePath: "/pages/runner/runner", text: "跑腿", icon: "🏃" },
      { pagePath: "/pages/messages/messages", text: "消息", icon: "💬" },
      { pagePath: "/pages/profile/profile", text: "我的", icon: "👤" }
    ]
  },

  methods: {
    switchTab(e) {
      var index = e.currentTarget.dataset.index;
      var item = this.data.list[index];
      wx.switchTab({ url: item.pagePath });
    }
  }
});
