import {
  PaginationConfig,
  TableColumnConfig,
  TableConfig,
} from "@/components/globalComponents/ElementTableC/table-component";
import { formItemConfig } from "./formConfig";

// 从表单配置中获取通知级别映射
const getNotificationLevelMap = () => {
  const levelConfig = formItemConfig[0].find(item => item.key === 'notificationLevel');
  const map: Record<string, string> = {};
  if (levelConfig && levelConfig.option) {
    levelConfig.option.forEach((item: any) => {
      map[item.value] = item.label;
    });
  }
  return map;
};

// 从表单配置中获取通知类型映射
const getNotificationTypeMap = () => {
  const typeConfig = formItemConfig[0].find(item => item.key === 'notificationType');
  const map: Record<string, string> = {};
  if (typeConfig && typeConfig.option) {
    typeConfig.option.forEach((item: any) => {
      map[item.value] = item.label;
    });
  }
  return map;
};

// 格式化通知级别
export const formatNotificationLevel = (level: string) => {
  const levelMap = getNotificationLevelMap();
  return levelMap[level] || level;
};

// 格式化通知类型
export const formatNotificationType = (type: string) => {
  const typeMap = getNotificationTypeMap();
  return typeMap[type] || type;
};

export let tableColumnConfig = ref<TableColumnConfig[]>([
  {
    label: "标题",
    prop: "title",
    width: "200",
  },
  {
    label: "内容",
    prop: "content",
    width: "300",
  },
  {
    label: "通知级别",
    prop: "notificationLevel",
    width: "100",
  },
  {
    label: "通知类型",
    prop: "notificationType",
    width: "100",
  },
  {
    label: "通知范围",
    prop: "userTypeText",
    width: "100",
  },
  {
    label: "用户ID",
    prop: "userId",
    width: "120",
  },
  {
    label: "状态",
    prop: "statusText",
    width: "80",
  },
  {
    label: "创建时间",
    prop: "formatCreatedAt",
    width: "160",
  },
  {
    label: "过期时间",
    prop: "formatExpiresAt",
    width: "160",
  },
  {
    label: "引用ID",
    prop: "referenceId",
    width: "100",
  },
  {
    label: "引用类型",
    prop: "referenceType",
    width: "100",
  },
  {
    label: '操作',
    prop: 'buttonSlot',
    contentSlot: true,
    width: '120',
  },
]);

export let tableConfig: TableConfig = {
  stripe: true,
  border: true,
  selection: true,
  size: "default",
  highlightCurrentRow: false,
};

export let tableData = ref<any[]>([]);

export let paginationConfig = ref<PaginationConfig>({
  total: 0,
  currentPage: 1,
  pageSize: 10,
  layout: "total, sizes, prev, pager, next, jumper",
  background: true,
});