const app = getApp();

Page({
  data: {
    userInfo: {},
    balance: 0,
    stats: {},
    addresses: []
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 3 });
    }
    this.refresh();
  },

  refresh() {
    var that = this;
    // 加载用户信息
    app.get("/api/user/profile").then(function (user) {
      that.setData({
        userInfo: user,
        balance: user.balance || 0,
        stats: {
          totalOrders: user.totalOrders || 0,
          completedOrders: user.completedOrders || user.totalOrders || 0,
          totalEarned: user.totalEarned || 0
        }
      });
    }).catch(function (err) {
      console.error("加载个人信息失败", err);
    });

    // 加载地址
    app.get("/api/addresses").then(function (addrs) {
      that.setData({ addresses: addrs || [] });
    }).catch(function (err) {
      console.error("加载地址失败", err);
    });
  },

  // 编辑昵称
  editProfile() {
    var that = this;
    wx.showModal({
      title: "修改昵称",
      editable: true,
      placeholderText: that.data.userInfo.nickname || "",
      success: function (res) {
        if (res.confirm && res.content) {
          app.put("/api/user/profile", { nickname: res.content }).then(function () {
            that.refresh();
            wx.showToast({ title: "已更新", icon: "success" });
          }).catch(function (err) {
            console.error("更新失败", err);
          });
        }
      }
    });
  },

  // 提现
  withdraw() {
    var that = this;
    if (that.data.balance <= 0) {
      wx.showToast({ title: "余额不足", icon: "none" });
      return;
    }
    wx.showModal({
      title: "模拟提现",
      editable: true,
      placeholderText: "输入金额",
      success: function (res) {
        if (res.confirm && res.content) {
          var amount = parseFloat(res.content);
          if (amount > 0 && amount <= that.data.balance) {
            app.post("/api/user/withdraw", { amount: amount }).then(function () {
              wx.showToast({ title: "已提现 ¥" + amount, icon: "success" });
              that.refresh();
            }).catch(function (err) {
              console.error("提现失败", err);
            });
          } else {
            wx.showToast({ title: "金额无效", icon: "none" });
          }
        }
      }
    });
  },

  // 地址管理
  addAddress() {
    this.editAddressModal(null);
  },

  editAddress(event) {
    var id = event.currentTarget.dataset.id;
    var addr = this.data.addresses.find(function (a) { return a.id === id; });
    this.editAddressModal(addr);
  },

  editAddressModal(addr) {
    var that = this;
    var isEdit = !!addr;
    wx.showModal({
      title: isEdit ? "编辑地址" : "新增地址",
      editable: true,
      placeholderText: isEdit
        ? (addr.tag || "") + "|" + (addr.detail || "") + "|" + (addr.contact || "") + "|" + (addr.phone || "")
        : "标签|详细地址|姓名|电话",
      success: function (res) {
        if (res.confirm && res.content) {
          var parts = res.content.split("|");
          if (parts.length >= 4) {
            var data = {
              tag: parts[0].trim(),
              detail: parts[1].trim(),
              contact: parts[2].trim(),
              phone: parts[3].trim()
            };
            var promise = isEdit
              ? app.put("/api/addresses/" + addr.id, data)
              : app.post("/api/addresses", data);
            promise.then(function () {
              that.refresh();
              wx.showToast({ title: isEdit ? "已更新" : "已添加", icon: "success" });
            }).catch(function (err) {
              console.error("操作失败", err);
            });
          } else {
            wx.showToast({ title: "格式：标签|地址|姓名|电话", icon: "none" });
          }
        }
      }
    });
  },

  deleteAddress(event) {
    var id = event.currentTarget.dataset.id;
    var that = this;
    wx.showModal({
      title: "确认删除",
      content: "确定要删除这个地址吗？",
      success: function (res) {
        if (res.confirm) {
          app.del("/api/addresses/" + id).then(function () {
            that.refresh();
            wx.showToast({ title: "已删除", icon: "success" });
          }).catch(function (err) {
            console.error("删除失败", err);
          });
        }
      }
    });
  },

  // 切换身份
  switchRole() {
    var that = this;
    var newRole = that.data.userInfo.role === "runner" ? "user" : "runner";
    wx.showModal({
      title: "切换身份",
      content: "确认切换为" + (newRole === "runner" ? "跑腿员" : "普通用户") + "？会重新登录。",
      success: function (res) {
        if (res.confirm) {
          app.switchRole(newRole).then(function (user) {
            that.setData({ userInfo: user });
            that.refresh();
            wx.showToast({ title: "已切换为" + newRole, icon: "success" });
          });
        }
      }
    });
  },

  goMyOrders() {
    wx.navigateTo({ url: "/pages/orders/orders" });
  },
  goRunner() {
    wx.switchTab({ url: "/pages/runner/runner" });
  },
  aboutApp() {
    wx.showModal({
      title: "小速达",
      content: "校内代取配送小程序。\n发布任务 → 跑腿员接单 → 取件拍照 → 送达拍照 → 完成。\n\n安全、便捷、仅限校园。",
      showCancel: false
    });
  }
});
