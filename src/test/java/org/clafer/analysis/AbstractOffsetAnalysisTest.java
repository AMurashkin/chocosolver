package org.clafer.analysis;

import java.util.Map;
import org.clafer.Scope;
import org.clafer.analysis.AbstractOffsetAnalysis.Offsets;
import org.clafer.ast.AstAbstractClafer;
import org.clafer.ast.AstClafer;
import org.clafer.ast.AstConcreteClafer;
import org.clafer.ast.AstModel;
import org.clafer.ast.Asts;
import org.clafer.ast.Card;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author jimmy
 */
public class AbstractOffsetAnalysisTest {

    @Test
    public void testAnalyze() {
        AstModel model = Asts.newModel();

        AstAbstractClafer object = model.addAbstractClafer("object");
        AstConcreteClafer id = object.addChild("id").withCard(new Card(1, 1));

        AstAbstractClafer animal = model.addAbstractClafer("animal").extending(object);
        AstAbstractClafer mammal = model.addAbstractClafer("mammal").extending(animal);
        AstAbstractClafer primate = model.addAbstractClafer("primate").extending(mammal);
        AstConcreteClafer arm = primate.addChild("arm").withCard(2, 2).extending(object);
        AstAbstractClafer human = model.addAbstractClafer("human").extending(primate);
        AstConcreteClafer jimmy = model.addTopClafer("Jimmy").extending(human).withCard(0, 1);
        AstConcreteClafer degree = jimmy.addChild("Degree@UWaterloo").extending(object).withCard(1, 1);
        AstConcreteClafer mona = model.addTopClafer("Mona").extending(human).withCard(1, 1);
        AstConcreteClafer lisa = model.addTopClafer("Lisa").extending(human).withCard(1, 1);
        AstConcreteClafer knut = model.addTopClafer("Knut").extending(mammal).withCard(0, 1);

        AstAbstractClafer art = model.addAbstractClafer("art").extending(object);
        AstConcreteClafer monalisa = model.addTopClafer("Mona Lisa").extending(art).withCard(1, 3); // forgeries

        Scope scope = new Scope(10);

        TypeHierarchyDepthAnalysis.analyze(model);
        Map<AstClafer, Card> globalCards = GlobalCardAnalysis.analyze(model, scope);
        Map<AstAbstractClafer, Offsets> offsetMap = AbstractOffsetAnalysis.analyze(model, globalCards);

        assertEquals(0, offsetMap.get(object).getOffset(object));
        assertEquals(offsetMap.get(object).getOffset(jimmy),
                offsetMap.get(object).getOffset(primate) + offsetMap.get(primate).getOffset(jimmy));
    }
}
