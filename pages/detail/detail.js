const app = getApp();

function getStatusKey(status) {
  var map = {
    "待接单": "pending",
    "已接单": "accepted",
    "已取件": "picked",
    "配送中": "delivering",
    "待确认": "confirming",
    "已完成": "done",
    "已取消": "cancelled"
  };
  return map[status] || "done";
}

function normalizeOrder(order, currentUserId) {
  var type = order.type || "订单";
  var userImages = order.userImages || order.images || [];
  return Object.assign({}, order, {
    title: order.title || (type + "取件"),
    type: type,
    from: order.from || order.pickupLocation,
    to: order.to || order.deliveryLocation,
    urgency: order.urgency || order.urgent || "普通",
    statusKey: order.statusKey || getStatusKey(order.status),
    userImages: userImages,
    pickupImages: order.pickupImages || [],
    deliveryImages: order.deliveryImages || [],
    timeline: order.timeline || [],
    // 下单人本人且订单待确认 → 显示确认按钮
    canConfirm: order.status === "待确认" && currentUserId != null && currentUserId === order.userId
  });
}

Page({
  data: {
    order: null,
    loading: false
  },

  onLoad(options) {
    this.orderId = options.id;
  },

  onShow() {
    this.loadOrder();
  },

  loadOrder() {
    var that = this;
    var userInfo = app.globalData.userInfo;
    var currentUserId = userInfo ? userInfo.id : null;
    that.setData({ loading: true });
    app.get("/api/orders/" + that.orderId).then(function (order) {
      that.setData({ order: normalizeOrder(order, currentUserId), loading: false });
    }).catch(function (err) {
      console.error("加载订单详情失败", err);
      that.setData({ loading: false });
    });
  },

  preview(event) {
    var list = event.currentTarget.dataset.list;
    wx.previewImage({
      current: event.currentTarget.dataset.src,
      urls: this.data.order[list]
    });
  },

  confirmOrder() {
    var that = this;
    wx.showModal({
      title: "确认完成",
      content: "确认订单已完成？报酬将会转到跑腿员余额。",
      success: function (res) {
        if (res.confirm) {
          app.post("/api/orders/" + that.orderId + "/confirm").then(function () {
            wx.showToast({ title: "已确认完成", icon: "success" });
            that.loadOrder();
          }).catch(function (err) {
            console.error("确认失败", err);
          });
        }
      }
    });
  }
});
