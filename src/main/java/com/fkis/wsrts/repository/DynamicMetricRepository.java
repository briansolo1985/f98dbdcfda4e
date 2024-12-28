package com.fkis.wsrts.repository;

import static java.util.Map.entry;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

import com.fkis.wsrts.repository.entity.SensorMetricEntity;
import com.fkis.wsrts.service.api.FilterField;
import com.fkis.wsrts.service.api.MetricField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CompoundSelection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaBuilder.Coalesce;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DynamicMetricRepository {

  private final EntityManager entityManager;

  public Map<String, Double> calculateAverages(List<String> sensorIds, List<MetricField> fields,
      long fromTimestamp, long toTimestamp) {
    if (fields == null || fields.isEmpty()) {
      return Map.of();
    }

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Object[]> query = criteriaBuilder.createQuery(Object[].class);

    Root<SensorMetricEntity> root = query.from(SensorMetricEntity.class);
    query.select(selectExpression(fields, criteriaBuilder, root));
    query.where(filterExpression(criteriaBuilder, root, sensorIds, fromTimestamp, toTimestamp));

    Object[] result = entityManager.createQuery(query).getSingleResult();

    return range(0, fields.size())
        .mapToObj(index -> entry(fields.get(index).getFieldName(), (Double) result[index]))
        .collect(toMap(Entry::getKey, Entry::getValue));
  }

  private CompoundSelection<Object[]> selectExpression(List<MetricField> fields,
      CriteriaBuilder criteriaBuilder, Root<SensorMetricEntity> root) {
    Expression[] averageExpressions = fields.stream()
        .map(field -> criteriaBuilder.avg(
            criteriaBuilder.<Double>coalesce().value(root.get(field.getFieldName())).value(0.0)
        ))
        .toArray(Expression[]::new);
    return criteriaBuilder.array(averageExpressions);
  }

  private Expression<Boolean> filterExpression(CriteriaBuilder criteriaBuilder,
      Root<SensorMetricEntity> root, List<String> sensorIds, long fromTimestamp, long toTimestamp) {
    Predicate rangePredicate = criteriaBuilder.between(
        root.get(FilterField.TIMESTAMP.getFieldName()),
        fromTimestamp,
        toTimestamp);

    Optional<Predicate> inPredicate = Optional.ofNullable(sensorIds)
        .filter(not(List::isEmpty))
        .map(ids -> {
          In<Object> in = criteriaBuilder.in(root.get(FilterField.SENSOR_ID.getFieldName()));
          ids.forEach(in::value);
          return in;
        });

    return inPredicate
        .map(predicate -> criteriaBuilder.and(predicate, rangePredicate))
        .orElse(rangePredicate);
  }
}
