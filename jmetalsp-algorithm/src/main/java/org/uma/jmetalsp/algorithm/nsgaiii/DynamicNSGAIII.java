package org.uma.jmetalsp.algorithm.nsgaiii;

import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetalsp.DynamicAlgorithm;
import org.uma.jmetalsp.DynamicProblem;
import org.uma.jmetalsp.observeddata.AlgorithmObservedData;
import org.uma.jmetalsp.observer.Observable;
import org.uma.jmetalsp.util.restartstrategy.RestartStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class implementing a dynamic version of NSGA-III. Most of the code of the original NSGA-III is
 * reused, and measures are used to allow external components to access the results of the
 * computation.
 *
 * @todo Explain the behaviour of the dynamic algorithm
 *
 * @author Cristobal Barba <cbarba@lcc.uma.es>
 */

public class DynamicNSGAIII  <S extends Solution<?>> extends NSGAIII<S>
        implements DynamicAlgorithm<List<S>, AlgorithmObservedData> {
    private int completedIterations ;
    private boolean stopAtTheEndOfTheCurrentIteration = false ;
    private RestartStrategy<S> restartStrategyForProblemChange ;

    Observable<AlgorithmObservedData> observable ;
    public DynamicNSGAIII(DynamicNSGAIIIBuilder builder,Observable<AlgorithmObservedData> observable) {
        super(builder);
        this.observable=observable;

    }

    @Override
    public DynamicProblem<?, ?> getDynamicProblem() {
        return (DynamicProblem<S, ?>) super.getProblem();
    }
    @Override
    protected boolean isStoppingConditionReached() {
        if (iterations >= maxIterations) {
            observable.setChanged() ;

            Map<String, Object> algorithmData = new HashMap<>() ;

            algorithmData.put("numberOfIterations",completedIterations);
            algorithmData.put("algorithmName", getName()) ;
            algorithmData.put("problemName", problem.getName()) ;
            algorithmData.put("numberOfObjectives", problem.getNumberOfObjectives()) ;

            observable.notifyObservers(new AlgorithmObservedData((List<Solution<?>>) getPopulation(), algorithmData));


            restart();
            evaluator.evaluate(getPopulation(), (Problem<S>) getDynamicProblem()) ;

            initProgress();
            completedIterations++;
        }
        return stopAtTheEndOfTheCurrentIteration ;
    }
    @Override protected void updateProgress() {
        if (getDynamicProblem().hasTheProblemBeenModified()) {
            restart();

            evaluator.evaluate(getPopulation(), (Problem<S>) getDynamicProblem()) ;
            getDynamicProblem().reset();
        }
        iterations += getMaxPopulationSize() ;
    }

    @Override
    public String getName() {
        return "DynamicNSGAIII";
    }

    @Override
    public String getDescription() {
        return "Dynamic version of algorithm NSGA-III";
    }

    @Override
    public Observable<AlgorithmObservedData> getObservable() {
        return this.observable ;
    }

    @Override
    public void restart() {
        this.restartStrategyForProblemChange.restart(getPopulation(), (DynamicProblem<S, ?>)getProblem());
    }

    @Override
    public void setRestartStrategy(RestartStrategy<?> restartStrategy) {
        this.restartStrategyForProblemChange = (RestartStrategy<S>) restartStrategy;
    }
}
