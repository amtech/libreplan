package org.zkoss.ganttz;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.zkoss.ganttz.TaskDetail.ITaskDetailNavigator;
import org.zkoss.ganttz.util.MutableTreeModel;
import org.zkoss.ganttz.util.TaskBean;
import org.zkoss.ganttz.util.TaskContainerBean;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.SimpleTreeNode;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeModel;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.TreeitemRenderer;

public class ListDetails extends HtmlMacroComponent {

    private final class TaskBeanRenderer implements TreeitemRenderer {
        public void render(Treeitem item, Object data) throws Exception {
            TaskBean taskBean = (TaskBean) data;
            item.setOpen(isOpened(taskBean));
            final int[] path = tasksTreeModel.getPath(tasksTreeModel.getRoot(),
                    taskBean);
            String cssClass = "depth_" + path.length;
            TaskDetail taskDetail = TaskDetail.create(taskBean,
                    new TreeNavigator(tasksTreeModel, path));
            if (taskBean instanceof TaskContainerBean) {
                expandWhenOpened((TaskContainerBean) taskBean, item);
            }
            Component row = Executions.getCurrent().createComponents(
                    "~./ganttz/zul/taskdetail.zul", item, null);
            taskDetail.doAfterCompose(row);
            List<Object> rowChildren = row.getChildren();
            List<Treecell> treeCells = Planner.findComponentsOfType(
                    Treecell.class, rowChildren);
            for (Treecell cell : treeCells) {
                cell.setSclass(cssClass);
            }
            detailsForBeans.put(taskBean, taskDetail);
        }

        private void expandWhenOpened(final TaskContainerBean taskBean,
                Treeitem item) {
            item.addEventListener("onOpen", new EventListener() {
                @Override
                public void onEvent(Event event) throws Exception {
                    OpenEvent openEvent = (OpenEvent) event;
                    taskBean.setExpanded(openEvent.isOpen());
                }
            });
        }

    }

    public boolean isOpened(TaskBean taskBean) {
        if (taskBean instanceof TaskContainerBean) {
            TaskContainerBean container = (TaskContainerBean) taskBean;
            return container.isExpanded();
        }
        return true;
    }

    private Map<TaskBean, TaskDetail> detailsForBeans = new HashMap<TaskBean, TaskDetail>();

    private final class TreeNavigator implements ITaskDetailNavigator {
        private final int[] pathToNode;

        private TreeNavigator(TreeModel treemodel, int[] pathToNode) {
            this.pathToNode = pathToNode;
        }

        @Override
        public TaskDetail getAboveDetail() {
            TaskBean parent = getParent(pathToNode);
            int lastPosition = pathToNode[pathToNode.length - 1];
            if (lastPosition != 0) {
                return getChild(parent, lastPosition - 1);
            } else if (tasksTreeModel.getRoot() != parent) {
                return detailsForBeans.get(parent);
            }
            return null;
        }

        private TaskDetail getChild(TaskBean parent, int position) {
            TaskBean child = tasksTreeModel.getChild(parent, position);
            return detailsForBeans.get(child);
        }

        @Override
        public TaskDetail getBelowDetail() {
            TaskBean parent = getParent(pathToNode);
            int childCount = tasksTreeModel.getChildCount(parent);
            int lastPosition = pathToNode[pathToNode.length - 1];
            int belowPosition = lastPosition + 1;
            if (belowPosition < childCount) {
                return getChild(parent, belowPosition);
            }
            return null;
        }

        private TaskBean getParent(int[] path) {
            TaskBean current = tasksTreeModel.getRoot();
            for (int i = 0; i < path.length - 1; i++) {
                current = tasksTreeModel.getChild(current, path[i]);
            }
            return current;
        }

    }

    private static Log LOG = LogFactory.getLog(ListDetails.class);

    private TaskRemovedListener taskRemovedListener;

    private final List<TaskBean> taskBeans;

    private MutableTreeModel<TaskBean> tasksTreeModel;

    private Tree tasksTree;

    public ListDetails(List<TaskBean> taskBeans) {
        this.taskBeans = taskBeans;
    }

    private static TaskBean getTaskBean(SimpleTreeNode node) {
        return (TaskBean) node.getData();
    }

    private static void fillModel(MutableTreeModel<TaskBean> treeModel,
            List<TaskBean> taskBeans) {
        for (TaskBean taskBean : taskBeans) {
            fillModel(treeModel, treeModel.getRoot(), taskBean);
        }
    }

    private static void fillModel(MutableTreeModel<TaskBean> treeModel,
            TaskBean parent, TaskBean node) {
        treeModel.add(parent, node);
        if (node instanceof TaskContainerBean) {
            TaskContainerBean container = (TaskContainerBean) node;
            for (TaskBean child : container.getTasks()) {
                fillModel(treeModel, container, child);
            }
        }
    }

    Planner getPlanner() {
        return (Planner) getParent();
    }

    public void taskRemoved(TaskBean taskRemoved) {
        // TODO pending
    }

    public void addTask() {
        TaskBean newTask = new TaskBean();
        newTask.setName("Nova Tarefa");
        newTask.setBeginDate(new Date());
        newTask.setEndDate(threeMonthsLater(newTask.getBeginDate()));
        addTask(newTask);
        getPlanner().addTask(newTask);
    }

    public void addTaskContainer() {
        TaskContainerBean newTask = new TaskContainerBean();
        newTask.setName("Novo Contedor de Tarefas");
        newTask.setBeginDate(new Date());
        newTask.setEndDate(threeMonthsLater(newTask.getBeginDate()));
        addTask(newTask);
        getPlanner().addTask(newTask);
    }

    private static Date threeMonthsLater(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 3);
        return calendar.getTime();
    }

    @Override
    public void afterCompose() {
        setClass("listdetails");
        super.afterCompose();
        tasksTree = (Tree) getFellow("tasksTree");
        tasksTreeModel = MutableTreeModel.create(TaskBean.class);
        fillModel(tasksTreeModel, taskBeans);
        tasksTree.setModel(tasksTreeModel);
        tasksTree.setTreeitemRenderer(new TaskBeanRenderer());
    }

    private void addTask(TaskBean taskBean) {
        tasksTreeModel.add(tasksTreeModel.getRoot(), taskBean);
    }

}
