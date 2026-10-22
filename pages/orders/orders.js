const app = getApp();

var TYPE_ICONS = {
  "快递": "快",
  "外卖": "餐",
  "文件": "文",
  "钥匙": "钥",
  "代购": "购",
  "其他": "其"
};

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

function getUrgentKey(urgent) {
  return urgent === "加急" || urgent === "紧急" || urgent === "极速" ? "urgent" : "normal";
}

function formatOrder(item) {
  var type = item.type || "订单";
  var urgency = item.urgency || item.urgent || "普通";
  return {
    id: item.id,
    orderNo: item.orderNo,
    type: type,
    typeIcon: item.typeIcon || TYPE_ICONS[type] || "其",
    title: item.title || (type + "取件"),
    from: item.from || item.pickupLocation,
    to: item.to || item.deliveryLocation,
    status: item.status,
    statusKey: item.statusKey || getStatusKey(item.status),
    urgency: urgency,
    urgent: urgency,
    urgentKey: item.urgentKey || getUrgentKey(urgency),
    fee: item.fee,
    contact: item.contact,
    pickupCode: item.pickupCode,
    note: item.note
  };
}

Page({
  data: {
    filters: ["全部", "待接单", "进行中", "已完成"],
    activeFilter: "全部",
    displayOrders: [],
    loading: false
  },

  onShow() {
    this.refresh();
  },

  setFilter(event) {
    this.setData({ activeFilter: event.currentTarget.dataset.value });
    this.refresh();
  },

  refresh() {
    var that = this;
    that.setData({ loading: true });
    var status = that.data.activeFilter;
    app.get("/api/orders", { status: status, page: 1, size: 50, mine: true }).then(function (data) {
      var displayOrders = (data.records || []).map(formatOrder);
      that.setData({ displayOrders: displayOrders, loading: false });
    }).catch(function (err) {
      console.error("加载订单失败", err);
      that.setData({ loading: false });
    });
  },

  goDetail(event) {
    wx.navigateTo({ url: "/pages/detail/detail?id=" + event.currentTarget.dataset.id });
  },

  goCreate() {
    wx.navigateTo({ url: "/pages/create/create" });
  }
});
