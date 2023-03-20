package cc.cc1234.common.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageData<T> {

    @Builder.Default
    private List<T> records = new ArrayList<>();

    private Long total;

    public static <T> PageData<T> of(List<T> records) {
        return PageData.<T>builder()
            .records(records)
            .build();
    }

    /**
     * @param total   总数据量
     * @param records 数据
     * @param <T>     数据类型
     */
    public static <T> PageData<T> of(List<T> records, Long total) {
        return PageData.<T>builder()
            .total(total)
            .records(records)
            .build();
    }

    public <R> PageData<R> map(Function<? super T, ? extends R> recordsMapper) {
        List<R> records = this.records
            .stream()
            .map(recordsMapper)
            .collect(Collectors.toList());
        return PageData.<R>builder()
            .total(this.total)
            .records(records)
            .build();
    }
}
