importClass(Packages.org.clafer.ast.Asts);

var scope = rc.setScope.bind(rc);
var defaultScope = rc.setDefaultScope.bind(rc);
var intRange = rc.setIntRange.bind(rc);
var Clafer = rc.getModel().addChild.bind(rc.getModel());
var Abstract = rc.getModel().addAbstract.bind(rc.getModel()); // Some Javascript engine versions has "abstract" as a keyword.
var Constraint = rc.getModel().addConstraint.bind(rc.getModel());

var Int = Asts.IntType; // Some Javascript engine versions has "int" as a keyword.
var Bool = Asts.BoolType;

var $this = Asts.$this;
var global = Asts.global;
var constant = Asts.constant;
var join = Asts.join;
var joinParent = Asts.joinParent;
var joinRef = Asts.joinRef;
var not = Asts.not;
var minus = Asts.minus;
var card = Asts.card;
var test = Asts.test;
var equal = Asts.equal;
var notEqual = Asts.notEqual;
var lessThan = Asts.lessThan;
var lessThanEqual = Asts.lessThanEqual;
var greaterThan = Asts.greaterThan;
var greaterThanEqual = Asts.greaterThanEqual;
var arithm = Asts.arithm;
function add(a1, a2) {
    return Asts.add([a1, a2]);
}
function sub(s1, s2) {
    return Asts.sub([s1, s2]);
}
function mul(m1, m2) {
    return Asts.mul([m1, m2]);
}
function div(d1, d2) {
    return Asts.div([d1, d2]);
}
function and(a1, a2) {
    return Asts.and([a1, a2]);
}
function ifOnlyIf(i1, i2) {
    return Asts.ifOnlyIf([i1, i2]);
}
function implies(i1, i2) {
    return Asts.implies([i1, i2]);
}
function or(o1, o2) {
    return Asts.or([o1, o2]);
}
function xor(x1, x2) {
    return Asts.xor([x1, x2]);
}
function union(u1, u2) {
    return Asts.union([u1, u2]);
}
function diff(d1, d2) {
    return Asts.diff([d1, d2]);
}
function inter(i1, i2) {
    return Asts.inter([i1, i2]);
}
var membership = Asts.membership;
var ifThenElse = Asts.ifThenElse;
var $in = Asts["in"];
var notIn = Asts.notIn;
var inter = Asts.inter;
var upcast = Asts.upcast;
var local = Asts.local;
var decl = Asts.decl;
var disjDecl = Asts.disjDecl;
var quantify = Asts.quantify;
var all = Asts.all;
var lone = Asts.lone;
var none = Asts.none;
var one = Asts.one;
var some = Asts.some;
