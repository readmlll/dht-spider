package top.readm.demo.dhtnetwork.dht.pojo;

import lombok.*;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
@ToString
//充当Integer的指针
public class IntegerPtr{
    public Integer integer;
}
