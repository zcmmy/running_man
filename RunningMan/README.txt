具体API实现清单
用户相关 (6个API)
POST /api/auth/login

POST /api/auth/register

GET /api/user/profile

PUT /api/user/profile

GET /api/user/stats

POST /api/user/logout

任务相关 (8个API)
GET /api/tasks (列表)

GET /api/tasks/{id} (详情)

POST /api/tasks (发布)

POST /api/tasks/{id}/accept (接单)

PUT /api/tasks/{id} (修改)

DELETE /api/tasks/{id} (删除)

GET /api/tasks/categories (分类)

GET /api/tasks/search (搜索)

订单相关 (10个API)
GET /api/orders/published (发布的)

GET /api/orders/accepted (接单的)

GET /api/orders/{id} (详情)

POST /api/orders/{id}/accept (接单)

POST /api/orders/{id}/complete (完成)

POST /api/orders/{id}/cancel (取消)

GET /api/orders/stats (统计)

GET /api/orders/current (当前订单)

GET /api/orders/{id}/tracking (跟踪)

PUT /api/orders/{id}/location (更新位置)

消息相关 (8个API)
GET /api/chats/sessions (会话列表)

GET /api/chats/{orderId}/messages (消息历史)

POST /api/chats/{orderId}/messages (发送消息)

GET /api/messages/system (系统消息)

POST /api/messages/{id}/read (标记已读)

DELETE /api/messages/{id} (删除消息)

GET /api/notifications (通知列表)

POST /api/notifications/read-all (全部已读)

搜索相关 (4个API)
GET /api/search/history (搜索历史)

POST /api/search/history (添加历史)

DELETE /api/search/history/{id} (删除历史)

DELETE /api/search/history (清空历史)