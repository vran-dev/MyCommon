package cc.cc1234.common.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class Sort {

    public static final Sort EMPTY = new Sort();

    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void addOrder(String field, Direction direction) {
        orders.add(new Order(field, direction));
    }

    public Sort map(Function<? super Order, ? extends Order> mapper) {
        List<Order> orders = this.orders
            .stream()
            .map(mapper)
            .collect(Collectors.toList());
        Sort sort = new Sort();
        sort.setOrders(orders);
        return sort;
    }

    public String toString(Function<List<Order>, String> mapper) {
        return mapper.apply(orders);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {

        private String field;

        private Direction direction;

    }

    public enum Direction {
        ASC, DESC;

        public static Direction of(String direction) {
            try {
                return Direction.valueOf(direction.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("invalid direction value: "
                    + direction
                    + ", must be one of: ASC, DESC");
            }
        }
    }
}
