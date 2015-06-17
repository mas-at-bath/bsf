#!/usr/bin/python

#------------------------------------------------------------------------
# REVISION HISTORY
# add new entries here at the top tagged by date and initials
# 20121124 JAP: code for -t option
# 20121121 TL: fixed a bug at instal_error of function getVar()
# 20121121 JAP: added p1 and p2 parameters and associated logic
# 20121121 JAP: packaged output formatting into functions
# 20121119 JAP: made error checking more careful in p_generates
# 20121118 JAP: code for -o option, instal_print, instal_error and instal_warn
# 20121115 JAP: code for -i option
# 20121115 JAP: added warning for undeclared obligations
# 20121115 JAP: code for -d option
# 20121115 JAP: code for command line arguments
# 20121115 TL: added code to handle use of undeclared obligations
# 20121114 JAP: started history and made up pre-history
# 20121114 JAP: Added standard prelude text and code to output it
# 20121108 TL: Fixed three bugs
#------------------------------------------------------------------------

from __future__ import print_function
import re
import sys
import argparse

sys.path.insert(0,"../..")

if sys.version_info[0] >= 3:
    raw_input = input

instal_output = sys.stdout

def instal_print(p): print(p,file=instal_output)

def instal_error(p): print(p,file=sys.stderr)

def instal_warn(p): print(p,file=sys.stderr)

#------------------------------------------------------------------------
# LEXER + PARSER for instal

reserved = {
#    'and'         : 'AND',
    'create'      : 'CREATE',
    'dissolve'    : 'DISSOLVE',
    'event'       : 'EVENT',
    'exogenous'   : 'EXOGENOUS',
    'fluent'      : 'FLUENT',
    'generates'   : 'GENERATES',
    'if'          : 'IF',
    'initially'   : 'INITIALLY',
    'initiates'   : 'INITIATES',
    'inst'        : 'INST',
    'institution' : 'INSTITUTION',
    'noninertial' : 'NONINERTIAL',
    'not'         : 'NOT',
    'obl'         : 'OBL',
    'obligation'  : 'OBLIGATION',
    'perm'        : 'PERM',
    'pow'         : 'POW',
    'terminates'  : 'TERMINATES',
    'type'        : 'TYPE',
    'viol'        : 'VIOL',
    'violation'   : 'VIOLATION',
#    'with'        : 'WITH',
    'when'        : 'WHEN',
}

tokens =  ['NAME','TYPE_NAME','NUMBER','LPAR','RPAR','SEMI','COMMA'] + list(reserved.values())

# Tokens

t_SEMI  = r';'
t_COMMA  = r','
t_LPAR  = r'\('
t_RPAR  = r'\)'

def t_NAME(t):
    r'[a-z][a-zA-Z_0-9]*'
    t.type = reserved.get(t.value,'NAME')    # Check for reserved words
    return t

def t_TYPE_NAME(t):
    r'[A-Z][a-zA-Z_0-9]*'
    return t

def t_NUMBER(t):
    # note: numbers are parsed but not converted into integers
    r'\d+'
    # t.value = int(t.value)
    return t

t_ignore = " \t\r"

# Comments
def t_COMMENT(t):
    r'%.*'
    pass
    # No return value. Token discarded

def t_newline(t):
    r'\n+'
    t.lexer.lineno += t.value.count("\n")

def t_error(t):
    instal_error("Illegal character '%s'" % t.value[0])
    t.lexer.skip(1)

# Build the lexer
import ply.lex as lex
lex.lex()

# dictionaries
names = { }
types = { }
exevents = { }
inevents = { }
vievents = { }
crevents = { }
dievents = { }
fluents = { }
noninertial_fluents = { }
violation_fluents = { }
obligation_fluents = [ ]
generates = [ ]
initiates = [ ]
terminates = [ ]
noninertials = [ ]
initials = [ ]

# start = 'institution'

# def p_empty(p):
#     'empty :'
#     pass

def check_fluent(p):
    if not ((p[0] in fluents) or (p[0] in noninertial_fluents)):
        instal_error("% ERROR: Not a fluent in {x}".format(x=p)) # needs prettifying
        exit(-1)

def check_event(p):
    if not ((p[0] in exevents) or (p[0] in inevents) or (p[0] in vievents)):
        instal_error("% ERROR: Not an event in {x}".format(x=p)) # needs prettifying
#        exit(-1)

def check_in_or_vi_event(p):
    if not ((p[0] in inevents) or (p[0] in vievents)):
        instal_error("% ERROR: Not an institutional or violation event in {x}".format(x=p)) # needs prettifying
#        exit(-1)

def check_inevent(p):
    if not (p[0] in inevents):
        instal_error("% ERROR: Not an institutional event in {x}".format(x=p)) # needs prettifying
#        exit(-1)

def check_exevent(p):
    if not (p[0] in exevents):
        instal_error("% ERROR: Not an exogenous event in {x}".format(x=p)) # needs prettifying
        exit(-1)

def p_institution(p):
    """ institution : INSTITUTION NAME SEMI declaration_list
    """
    names["institution"] = p[2]

def p_declaration_list(p):
    """ declaration_list :
        declaration_list :  declaration_list declaration
    """
    if len(p)==2: p[0] = p[1] + [p[2]]

def p_declaration_type(p):
    """ declaration : TYPE TYPE_NAME SEMI
    """
    types[p[2]] = p[2].lower()

#
def p_declaration_event(p):
    """ declaration : exevent
        declaration : crevent
        declaration : dievent
        declaration : inevent
        declaration : vievent
        declaration : fluent_declaration
        declaration : noninertial_fluent
        declaration : violation_fluent
        declaration : obligation_fluent_declaration
        declaration : generates
        declaration : initiates
        declaration : terminates
        declaration : noninertial_rule
        declaration : initially
    """
    p[0] = [p[1]]

def p_type_name_list(p):
    """ type_name_list :
        type_name_list : TYPE_NAME
        type_name_list : TYPE_NAME COMMA type_name_list
    """
    if len(p)>2: p[0] = [p[1]] + p[3] # general case
    elif len(p)==2: p[0] = [p[1]]     # unary case
    # nullary case

# def p_name_list(p):
#     """ name_list :
#         name_list : NAME
#         name_list : NAME COMMA name_list
#     """
#     if len(p)>2: p[0] = [p[1]] + p[3] # general case
#     elif len(p)==2: p[0] = [p[1]]     # unary case
#     # nullary case

# def p_term(p):
#     """ term : NAME
#         term : NAME LPAR name_list RPAR
#     """
#     p[0] = [p[1],p[3]]

def p_typed_term(p):
    """ typed_term : NAME
        typed_term : NAME LPAR type_name_list RPAR
    """
    if len(p)>2:
        p[0] = [p[1],p[3]]
    else:
        p[0] = [p[1], []]

def p_typed_term_list(p):
    """ typed_term_list :
        typed_term_list : typed_term
        typed_term_list : typed_term COMMA typed_term_list
    """
    if len(p)>2: p[0] = [p[1]] + p[3] # general case
    elif len(p)==2: p[0] = [p[1]]     # unary case
    # nullary case

def p_extended_typed_term(p):
    """ extended_typed_term : violation_typed_term
        extended_typed_term : permission_typed_term
        extended_typed_term : power_typed_term
        extended_typed_term : typed_term
    """
#        extended_typed_term : obligation_typed_term
    p[0] = p[1]

# def p_extended_typed_term_list(p):
#     """ extended_typed_term_list :
#         extended_typed_term_list : extended_typed_term
#         extended_typed_term_list : extended_typed_term COMMA extended_typed_term_list
#     """
#     if len(p)>2: p[0] = [p[1]] + p[3] # general case
#     elif len(p)==2: p[0] = [p[1]]     # unary case
#     # nullary case

def p_violation_typed_term(p):
    """ violation_typed_term : VIOL LPAR typed_term RPAR
    """
    p[0] = [p[1],p[3]]

def p_permission_typed_term(p):
    """ permission_typed_term : PERM LPAR typed_term RPAR
    """
    p[0] = [p[1],p[3]]

def p_power_typed_term(p):
    """ power_typed_term : POW LPAR typed_term RPAR
    """
    p[0] = [p[1],p[3]]

# def p_obligation_typed_term(p):
#     """ obligation_typed_term : OBL LPAR typed_term COMMA typed_term COMMA typed_term RPAR
#     """
#     p[0] = [p[1],p[3],p[5],p[7]]

# def p_extended_term(p):
#     """ extended_term : violation_term
#         extended_term : permission_term
#         extended_term : power_term
#         extended_term : obligation_term
#         extended_term : term
#     """
#     p[0] = p[1]

# def p_violation_term(p):
#     """ violation_term : VIOL LPAR term RPAR
#     """
#     p[0] = [p[1],p[3]]

# def p_permission_term(p):
#     """ permission_term : PERM LPAR term RPAR
#     """
#     p[0] = [p[1],p[3]]

# def p_power_term(p):
#     """ power_term : POW LPAR term RPAR
#     """
#     p[0] = [p[1],p[3]]

# def p_obligation_term(p):
#     """ obligation_term : OBL LPAR term COMMA term COMMA term RPAR
#     """
#     p[0] = [p[1],p[3],p[5],p[7]]

# def p_extended_term_list(p):
#     """ extended_term_list :
#         extended_term_list : extended_term
#         extended_term_list : extended_term COMMA extended_term_list
#     """
#     if len(p)>2: p[0] = [p[1]] + p[3] # general case
#     elif len(p)==2: p[0] = [p[1]]     # unary case
#     # nullary case

def p_exevent(p):
    """ exevent : EXOGENOUS EVENT typed_term SEMI
    """
    event = p[3][0]
    args = p[3][1]
    exevents[event]=args
    p[0] = [p[1]]

def p_crevent(p):
    """ crevent : CREATE EVENT typed_term SEMI
    """
    event = p[3][0]
    args = p[3][1]
    crevents[event]=args
    p[0] = [p[1]]

def p_dievent(p):
    """ dievent : DISSOLVE EVENT typed_term SEMI
    """
    event = p[3][0]
    args = p[3][1]
    dievents[event]=args
    p[0] = [p[1]]

def p_inevent(p):
    """ inevent : INST EVENT typed_term SEMI
    """
    event = p[3][0]
    args = p[3][1]
    inevents[event]=args
    p[0] = [p[1]]

def p_vievent(p):
    """ vievent : VIOLATION EVENT typed_term SEMI
    """
    event = p[3][0]
    args = p[3][1]
    vievents[event]=args
    p[0] = [p[1]]

def p_fluent_declaration(p):
    """ fluent_declaration : FLUENT typed_term SEMI
    """
    fluent = p[2][0]
    args = p[2][1]
    fluents[fluent]=args
    p[0] = [p[1]]

def p_noninertial_fluent(p):
    """ noninertial_fluent : NONINERTIAL FLUENT typed_term SEMI
    """
    fluent = p[3][0]
    args = p[3][1]
    noninertial_fluents[fluent]=args
    p[0] = [p[1]]

def p_violation_fluent(p):
    """ violation_fluent : VIOLATION FLUENT typed_term SEMI
    """
    fluent = p[3][0]
    args = p[3][1]
    violation_fluents[fluent]=args
    p[0] = [p[1]]

def p_obligation_fluent_declaration(p):
    """ obligation_fluent_declaration : OBLIGATION FLUENT OBL LPAR typed_term COMMA typed_term COMMA typed_term RPAR SEMI
    """
    global obligation_fluents
    obligation_fluents = [[p[5],p[7],p[9]]] + obligation_fluents
    p[0] = [p[1]]

def violp(x): return x[0]=='viol'

# extended_typed_term is too permissive (I think)
# remind me to ask me why
def p_generates(p):
    """ generates : extended_typed_term GENERATES typed_term_list SEMI
        generates : extended_typed_term GENERATES typed_term_list condition SEMI
    """
    global generates
    exev = p[1]
    if violp(p[1]): # JAP 20121119 more careful error checking
        check_event(p[1][1])
    else:
        check_event(p[1])
    genev = p[3]
    for x in genev: # JAP 20121119 more careful error checking
        check_in_or_vi_event(x)
    cond = []
    if len(p)>5: # process conditions
        cond = p[4]
    generates = [[exev,genev,cond]] + generates
    p[0] = [p[1]]

def p_condition(p):
    """ condition : IF antecedent
    """
    p[0] = p[2]

def p_fluent(p):
    """ fluent : NAME
        fluent : POW LPAR fluent RPAR
        fluent : PERM LPAR fluent RPAR
        fluent : OBL LPAR fluent COMMA fluent COMMA fluent RPAR
        fluent : NAME LPAR fluent_arg_list RPAR
    """
    if len(p)==2:
        p[0] = [p[1], []]
    elif len(p)==5:
        p[0] = [p[1],p[3]]
    else: # must be an obligation
        p[0] = [p[1],[p[3],p[5],p[7]]]
        # TL: 20121115
        # if the obligation is not declared, but only occurs in rules
        # then it needs to be added to the list obligation_fluents explicitly
        # NOTE: the variable names may not be the standard ones, e.g. Agent1, Agent2
        # so standardize them (e.g. Agent1 -> Agent) by calling getVars() before added to obligation_fluents
        global obligation_fluents
        declared = False
        for of in obligation_fluents:
            [e, d, v] = of
            if e[0] == p[3][0] and d[0] == p[5][0] and v[0] == p[7][0]:
                declared = True
        if not declared:
            instal_warn("WARNING: obligation {obl} used but not declared\n"
                  .format(obl=p[0]))
            p[3][1] = getVars(p[3][0])
            p[5][1] = getVars(p[5][0])
            p[7][1] = getVars(p[7][0])
            obligation_fluents = obligation_fluents + [[p[3],p[5],p[7]]]

def p_fluent_arg_list(p):
    """ fluent_arg_list : NAME
        fluent_arg_list : TYPE_NAME
        fluent_arg_list : NUMBER
        fluent_arg_list : NAME COMMA fluent_arg_list
        fluent_arg_list : TYPE_NAME COMMA fluent_arg_list
        fluent_arg_list : NUMBER COMMA fluent_arg_list
    """
    if len(p)>2: p[0] = [p[1]] + p[3] # general case
    elif len(p)==2: p[0] = [p[1]]     # unary case
    # nullary case

def p_fluent_list(p):
    """ fluent_list :
        fluent_list : fluent
        fluent_list : fluent COMMA fluent_list
    """
    if len(p)==2: p[0] = [p[1]] # unary case
    else: p[0] = [p[1]] + p[3]  # other three

def p_antecedent(p):
    """ antecedent : antecedent COMMA antecedent
        antecedent : NOT fluent
        antecedent : fluent
    """
    # print p[1]
    if len(p)==2:
        check_fluent(p[1])
        p[0] = p[1]
    elif len(p)==3:
        check_fluent(p[2])
        p[0] = ['not',p[2]]
    else: p[0] = ['and',p[1],p[3]]

def p_initiates(p):
    """ initiates : typed_term INITIATES fluent_list SEMI
        initiates : typed_term INITIATES fluent_list condition SEMI
    """
    global initiates
    inev = p[1]
    inf = p[3]
    # print p[3]
    cond = []
    if len(p)>5: # process conditions
        cond = p[4]
    initiates = [[inev,inf,cond]] + initiates
    p[0] = [p[1]]

def p_terminates(p):
    """ terminates : typed_term TERMINATES fluent_list SEMI
        terminates : typed_term TERMINATES fluent_list condition SEMI
    """
    global terminates
    inev = p[1]
    inf = p[3]
    cond = []
    if len(p)>5: # process conditions
        cond = p[4]
    terminates = [[inev,inf,cond]] + terminates
    p[0] = [p[1]]

def p_noninertial_rule(p):
    """ noninertial_rule : typed_term WHEN antecedent SEMI
    """
    global noninertials
    nif = p[1]
    ante = p[3]
    noninertials = [[nif,ante]] + noninertials
    p[0] = [p[1]]

def p_initially_list(p):
    """ initially_list : fluent
        initially_list : fluent COMMA initially_list
    """
    global initials
    initials = initials + [p[1]]
    p[0] = [p[1]]

def p_initially(p):
    """ initially : INITIALLY initially_list SEMI
    """
    p[0] = p[2]

def p_error(p):
    if p:
        instal_error("Syntax error at '%s'" % p.value)
    else:
        instal_error("Syntax error at EOF")

def term2string(p):
    # print "term2string: p = ",p
    args = p[1]
    r=''
    if len(args)==0:
        r=p[0]
    elif len(args)==1:
        r=p[0]+'('+args[0]+')'
    else:
        r='('+args[0]
        for x in args[1:]: r=r+','+x
        r=p[0]+r+')'
    return r


def extendedterm2string(p):
    #if p[0] in ['perm','viol','pow']:
    #    r=p[0]+'('+term2string(p[1])+')'
    if p[0] in ['perm','viol']:
        r=p[0]+'('+term2string(p[1])+')'
    elif p[0] == 'pow':  # special handling for pow.-- TL: 20121108
        r=p[0]+'('+ names['institution']+','+term2string(p[1])+')'
    elif p[0] == 'obl':
        r=p[0]+'('+term2string(p[1][0])+','+term2string(p[1][1])+','+term2string(p[1][2])+')'
    else:
        r=term2string(p)
    return r

# Global state variable used by typecheck and args2string

saved_enumerator=0

def typecheck(p,cont=False):
    # used in processing event declarations and fluents when clearing the
    # institutional state
    # parameters must be unique so that the type predicates do
    # not over-constrain parameters, ie. multiple occurrences of the same
    # formal parameter do not lead to them being grounded to the same value
    # this depends on the same enumeration occurring in args2string
    # Sometimes, uniqueness is required across more than one term, so save
    # the counter and start from there if cont is True
    global saved_enumerator
    # if cont: print "typecheck: ",p,cont,saved_enumerator
    if p==[]: return 'true'
    if not cont: saved_enumerator=0
    i=saved_enumerator
    r=types[p[0]]+'('+p[0]+str(i)+')'
    for j,t in enumerate(p[1:]): r=r+','+types[t]+'('+t+str(i+j+1)+')'
    saved_enumerator=i+len(p)
    return r

def args2string(p,cont=False):
    # variables are enumerated to ensure uniqueness and is consistent with
    # that applied in typecheck.  Sometimes, uniqueness is required across
    # more than one term, so save the counter and start from there if cont
    # is True
    global saved_enumerator
    # if cont: print "args2string: ",p,cont,saved_enumerator
    if p==[]: return ''
    if not cont: saved_enumerator=0
    i=saved_enumerator
    r='('+p[0]+str(i)
    for j,x in enumerate(p[1:]): r=r+','+x+str(i+j+1)
    r=r+')'
    saved_enumerator=i+len(p)
    return r

def printCondition(c):
    # print "printCondition: c = ",c
    if c==[]: return
    if c[0]=='and':
        printCondition(c[1])
        printCondition(c[2])
    elif c[0]=='not':
        instal_print("   not")
        printCondition(c[1])
    else:
        instal_print("   holdsat({fluent},I),".format(
                fluent=term2string(c)))

def isVar(t):
    return t[0]<>t[0].lower()

def collectVars(t,d):
    # print "collectVars(top): t = ",t,"d = ",d
    if t==[]: return
    if t[0]=='and':
        # print "collectVars(and): t = ",t
        collectVars(t[1],d)
        collectVars(t[2],d)
    elif t[0]=='not':
        collectVars(t[1],d)
    elif t[0]=='obl':
        for x in t[1]: collectVars(x,d)
    else:
        if t[0] in ['perm','pow','viol']: t = t[1]
        op = t[0]
        args = t[1]
        # print "op = ", op, "args = ",args
        # not considering exogenous or creation events or violation fluents
        for evd in [exevents,inevents,vievents,fluents,noninertial_fluents,obligation_fluents]:
            if op in evd:
                for (t1,t2) in zip(evd[op],args):
                    if t2 in d:
                        if t1<>d[t2]:
                            instal_error("% ERROR: {v} has type {t1} and type {t2}".format(v=t2,t1=t1,t2=d[t2]))
                    # only remember t2 if it is a variable (not a literal)
                    if isVar(t2): d[t2] = t1
                # print "after collectVars: d = ",d
                return # assume that once found can stop searching
        # reaching here means the term was not in any of the tables
        instal_warn("% WARNING: {t} not found in collectVars".format(t=t))

# TL: 20121115
# returns the standard variable names given the event/fluent name h
def getVars(h):
    if inevents.has_key(h):
        varH = inevents[h]
    elif exevents.has_key(h):
        varH = exevents[h]
    elif vievents.has_key(h):
        varH = vievents[h]
    elif fluents.has_key(h):
        varH = fluents[h]
    else:
        # TL: 20121121 instal_error only takes one argument
        instal_error("ERROR: Unknown Type of {h}".format(h=h))
        #instal_error("ERROR: Unknown Type of", h)
        return
    return varH

#------------------------------------------------------------------------
# JAP: 20121121
# output formatting functions

def instal_print_standard_prelude():
    # JAP: 2012114
    # Printing of standard prelude should be conditional on whether
    # we are extending an existing definition or not.  For now, that
    # option is not supported.
    instal_print("%\n% Standard prelude for {institution}\n%".format(**names))
    instal_print(standard_prelude)
    instal_print("%\n% Rules for Institution {institution}\n%\n"
                 "  ifluent(live({institution})).\n"
                 "  inst({institution}).".format(**names))

def instal_print_types():
    # print types
    instal_print("%\n% The following types were declared:\n%")
    for t in types: instal_print("% {x}".format(x=t))

def instal_print_exevents():
    # print exevents
    instal_print("%\n% Exogenous events")
    for ev in exevents:
        instal_print("% Event: {ev} (type: ex)\n"
                     "  event({ev}{args}) :- {rhs}.\n" #args1(Arg1) etc.
                     "  evtype({ev}{args},ex) :- {rhs}.\n"
                     "  evinst({ev}{args},{inst}) :- {rhs}.\n"
                     "  ifluent(perm({ev}{args})) :- {rhs}.\n"
                     "  event(viol({ev}{args})) :- {rhs}.\n"
                     "  evtype(viol({ev}{args}),viol) :- {rhs}.\n"
                     "  evinst(viol({ev}{args}),{inst}) :- {rhs}."
                     .format(ev=ev,args=args2string(exevents[ev]),
                             rhs=typecheck(exevents[ev]),inst=names["institution"]))

def instal_print_inevents():
    # print inevents
    instal_print("% Institutional events")
    for ev in inevents:
        instal_print("% Event: {ev} (type: in)\n"
                     "  event({ev}{args}) :- {rhs}.\n" # as above
                     "  evtype({ev}{args},inst) :- {rhs}.\n"
                     "  evinst({ev}{args},{inst}) :- {rhs}.\n"
                     "  ifluent(pow({inst},{ev}{args})) :- {rhs}.\n"
                     "  ifluent(perm({ev}{args})) :- {rhs}.\n"
                     "  event(viol({ev}{args})) :- {rhs}.\n"
                     "  evtype(viol({ev}{args}),viol) :- {rhs}.\n"
                     "  evinst(viol({ev}{args}),{inst}) :- {rhs}."
                     .format(ev=ev,args=args2string(inevents[ev]),
                             rhs=typecheck(inevents[ev]),inst=names["institution"]))

def instal_print_vievents():
    # print vievents
    instal_print("%\n% Violation events\n%")
    for ev in vievents:
        instal_print("% Event: {ev} (type: in)\n"
                     "  event({ev}{args}) :- {rhs}.\n"
                     "  evtype({ev}{args},viol) :- {rhs}.\n"
                     "  evinst({ev}{args},{inst}) :- {rhs}."
                     .format(ev=ev,args=args2string(vievents[ev]),
                             rhs=typecheck(vievents[ev]),inst=names["institution"]))

def instal_print_crevents():
    # print crevents
    instal_print("%\n% Creation events\n%")
    for ev in crevents:
        instal_print("% Event: {ev} (type: ex)\n"
                     "  event({ev}{args}) :- {rhs}.\n" #args1(Arg1) etc.
                     "  evtype({ev}{args},ex) :- {rhs}.\n"
                     "  evinst({ev}{args},{inst}) :- {rhs}.\n"
                     "  ifluent(perm({ev}{args})) :- {rhs}.\n"
                     "  event(viol({ev}{args})) :- {rhs}.\n"
                     "  evtype(viol({ev}{args}),viol) :- {rhs}.\n"
                     "  evinst(viol({ev}{args}),{inst}) :- {rhs}."
                     .format(ev=ev,args=args2string(crevents[ev]),
                             rhs=typecheck(crevents[ev]),inst=names["institution"]))

def instal_print_dievents():
    # print dievents
    instal_print("%\n% Dissolution events\n%")
    for ev in dievents:
        instal_print("% Event: {ev} (type: ex)\n"
                     "  event({ev}{args}) :- {rhs}.\n" #args1(Arg1) etc.
                     "  evtype({ev}{args},ex) :- {rhs}.\n"
                     "  evinst({ev}{args},{inst}) :- {rhs}.\n"
                     "  ifluent(perm({ev}{args})) :- {rhs}.\n"
                     "  event(viol({ev}{args})) :- {rhs}.\n"
                     "  evtype(viol({ev}{args}),viol) :- {rhs}.\n"
                     "  evinst(viol({ev}{args}),{inst}) :- {rhs}."
                     .format(ev=ev,args=args2string(dievents[ev]),
                             rhs=typecheck(dievents[ev]),inst=names["institution"]))

def instal_print_inertial_fluents():
    # inertial fluents
    instal_print("%\n% inertial fluents\n%")
    for inf in fluents:
        instal_print("ifluent({name}) :-".format(name=term2string([inf,fluents[inf]])))
        for t in fluents[inf]:
            instal_print("   {pred}({tvar}),".format(pred=t.lower(),tvar=t))
        instal_print("   true.")

def instal_print_noninertial_fluents():
    # noninertial fluents
    instal_print("%\n% noninertial fluents\n%")
    for nif in noninertial_fluents:
        instal_print("nifluent({name}) :-".format(name=term2string([nif,noninertial_fluents[nif]])))
        for t in noninertial_fluents[nif]:
            instal_print("   {pred}({tvar}),".format(pred=t.lower(),tvar=t))
        instal_print("   true.")

def instal_print_violation_fluents():
    # violation fluents
    instal_print("%\n% violation fluents (to be implemented)\n")
    # for vf in fluents:
    #     print "ifluent({name}).".format(name=vf)

def instal_print_obligation_fluents():
    # obligation fluents
    instal_print("%\n% obligation fluents\n%")
    for of in obligation_fluents:
        # e=term2string(of[0])
        # d=term2string(of[1])
        # v=term2string(of[2])
        e=of[0][0]+args2string(of[0][1])
        d=of[1][0]+args2string(of[1][1],cont=True)
        v=of[2][0]+args2string(of[2][1],cont=True)
        te=typecheck(of[0][1])
        td=typecheck(of[1][1],cont=True)
        tv=typecheck(of[2][1],cont=True)
        instal_print("oblfluent(obl({e},{d},{v})) :-\n"
                     "   event({e};{d};{v}),\n"
                     "   {te},{td},{tv}."
                     .format(e=e,d=d,v=v,te=te,td=td,tv=tv))
        instal_print("terminated(obl({e},{d},{v}),I) :-\n"
                     "   occurred({e},I),\n"
                     "   holdsat(obl({e},{d},{v}),I),\n"
                     "   event({e};{d};{v}),\n"
                     "   {te},{td},{tv},instant(I)."
                     .format(e=e,d=d,v=v,te=te,td=td,tv=tv))
        instal_print("terminated(obl({e},{d},{v}),I) :-\n"
                     "   occurred({d},I),\n"
                     "   holdsat(obl({e},{d},{v}),I),\n"
                     "   event({e};{d};{v}),\n"
                     "   {te},{td},{tv},instant(I)."
                     .format(e=e,d=d,v=v,te=te,td=td,tv=tv))
        instal_print("occurred({v},I) :-\n"
                     "   occurred({d},I),\n"
                     "   holdsat(obl({e},{d},{v}),I),\n"
                     "   event({e};{d};{v}),\n"
                     "   {te},{td},{tv},instant(I)."
                     .format(e=e,d=d,v=v,te=te,td=td,tv=tv))

def instal_print_generates():
    # generates
    instal_print("%\n% generate rules\n%")
    for rl in generates:
        [inorexev,inev,cond] = rl
        vars1 = {}
        collectVars(inorexev,vars1)
        collectVars(cond,vars1)
        for x in inev:
            vars2 = {}
            collectVars(x,vars2)
            instal_print("%\n"
                         "% Translation of {exev} generates {inev} if {condition}\n"
                         "occurred({inev},I) :- occurred({exev},I),\n"
                         "   holdsat(pow({inst},{inev}),I),"
                         .format(exev=extendedterm2string(inorexev),
                                 inev=extendedterm2string(x),
                                 inst=names["institution"],
                                 condition=cond))
            printCondition(cond)
            for k in vars1:
                instal_print("   {pred}({tvar}),".format(pred=types[vars1[k]],tvar=k))
            for k in vars2:
                # should check for consistent usage of k in vars1 and vars2
                if k not in vars1:
                    instal_print("   {pred}({tvar}),".format(pred=types[vars2[k]],tvar=k))
            instal_print("   instant(I).")

def instal_print_initiates():
    # initiates
    instal_print("%\n% initiate rules\n%")
    for rl in initiates:
        [inev,inits,cond] = rl
        vars1 = {}
        collectVars(inev,vars1)
        collectVars(cond,vars1)
        for x in inits:
            vars2 = {}
            collectVars(x,vars2)
            instal_print("%\n% Translation of {inev} initiates {inits} if {condition}"
                         .format(inev=term2string(inev),inits=x,condition=cond))
            instal_print("%\ninitiated({inf},I) :-\n"
                         "   occurred({ev},I),\n"
                         "   holdsat(live({inst}),I),"
                         .format(inf=extendedterm2string(x),
                                 ev=term2string(inev),
                                 inst=names["institution"]))
            printCondition(cond)
            for k in vars1:
                instal_print("   {pred}({tvar}),".format(pred=types[vars1[k]],tvar=k))
            for k in vars2:
                # should check for consistent usage of k in vars1 and vars2
                if k not in vars1:
                    instal_print("   {pred}({tvar}),".format(pred=types[vars2[k]],tvar=k))
            instal_print("   instant(I).")

def instal_print_terminates():
    # terminates
    instal_print("%\n% terminate rules\n%")
    for rl in terminates:
        [inev,terms,cond] = rl
        vars1 = {}
        collectVars(inev,vars1)
        collectVars(cond,vars1)
        for x in terms: # TL: 20121108
            vars2 = {}
            collectVars(x,vars2)
            instal_print("%\n% Translation of {inev} terminates {terms} if {condition}"
                         .format(inev=term2string(inev),terms=x,condition=cond))
            instal_print("%\nterminated({inf},I) :-\n"
                         "   occurred({ev},I),\n"
                         "   holdsat(live({inst}),I),"
                         .format(inf=extendedterm2string(x),
                                 ev=term2string(inev),
                                 inst=names["institution"]))
            printCondition(cond)
            for k in vars1:
                instal_print("   {pred}({tvar}),".format(pred=types[vars1[k]],tvar=k))
            for k in vars2:
                # should check for consistent usage of k in vars1 and vars2
                if k not in vars1:
                    instal_print("   {pred}({tvar}),".format(pred=types[vars2[k]],tvar=k))
            instal_print("   instant(I).")

def instal_print_noninertials():
    # noninertials
    instal_print("%\n% noninertial rules\n%")
    for rl in noninertials:
        [nif,ante] = rl
        vars1 = {}
        collectVars(nif,vars1)
        instal_print("%\n% Translation of {nif} when {ante}\n"
                     "holdsat({nif},I) :-"
                     .format(nif=term2string(nif),ante=ante))
        printCondition(ante)
        collectVars(ante,vars1)
        for k in vars1:
            instal_print("   {pred}({tvar}),".format(pred=types[vars1[k]],tvar=k))
        instal_print("   instant(I).")

def instal_print_initially():
    # initially
    instal_print("%\n% initially\n%")
    # note this needs revision to time.lp
    if len(crevents) == 0:
        instal_print("% no creation event")
        instal_print("holdsat(live({inst}),I) :- start(I)."
                     .format(inst=names["institution"]))
        for i in initials:
            fvars = {}
            instal_print("% initially: {x}".format(x=extendedterm2string(i)))
            instal_print("holdsat({inf},I) :-".format(inf=extendedterm2string(i)))
            collectVars(i,fvars)
            for k in fvars:
                instal_print("   {pred}({tvar}),".format(pred=types[fvars[k]],tvar=k))
            instal_print("   start(I).")
    else:
        instal_print("% at least one create event")
        instal_print("%\n% clear state to allow for re-creation\n%")
        # terminate(f,I) :- occurred(create{name},I), holdsat(f,I), instant(I), type(...) because f may be a fluent
        for c in crevents:
            instal_print("terminated(F,I) :-\n"
                         "   occurred({cev},I),\n"
                         "   not holdsat(live({inst}),I),\n"
                         "   holdsat(F,I),\n"
                         "   instant(I)."
                         .format(cev=c,
                                 inst=names["institution"]))
            # to add the effect of creation event:
            # initiated(live(inst_1),I) :- occurred(logisticsStartEU,I),not holdsat(live(inst_1),I),instant(I). -- TL: 20121108
            instal_print("initiated(live({inst}),I) :-\n"
                         "   occurred({cev},I),\n"
                         "   not holdsat(live({inst}),I),\n"
                         "   instant(I)."
                         .format(cev=c,
                                 inst=names["institution"]))
            # process the initials
            instal_print("%\n% set up initial state\n%")
            for c in crevents:
                for i in initials:
                    fvars = {}
                    instal_print("% initially: {x}".format(x=i)) #extendedterm2string(i)
                    instal_print("initiated({inf},I) :-\n"
                                 "   occurred({cname},I),\n"
                                 "   not holdsat(live({inst}),I),"
                                 .format(cname=c,
                                         inf=extendedterm2string(i),
                                         inst=names["institution"]))
                    collectVars(i,fvars)
                    for k in fvars:
                        instal_print("   {pred}({tvar}),".format(pred=types[fvars[k]],tvar=k))
                    instal_print("   instant(I).")

def instal_print_dissolve():
    # dissolve
    instal_print("%\n% dissolve events\n%")
    for d in dievents:
        instal_print("terminated(live({inst}),I) :-\n"
                     "   occurred({dev},I),\n"
                     "   {args}, % true if dissolve event has no parameters"
                     .format(dev=d,
                             args=typecheck(dievents[d]),
                             inst=names["institution"]))
        instal_print("   instant(I).")

#------------------------------------------------------------------------

import ply.yacc as yacc
yacc.yacc()

# JAP: 20121114
standard_prelude = "\
% instant ordering\n\
before(I1,I2):- next(I1,I2),instant(I1), instant(I2).\n\
before(I1,I3):- before(I1,I2),before(I2,I3),\n\
   instant(I1),instant(I2),instant(I3).\n\
% fluent rules\n\
holdsat(P,J):- holdsat(P,I),not terminated(P,I),\n\
    next(I,J),ifluent(P),instant(I),instant(J).\n\
holdsat(P,J):- initiated(P,I),next(I,J),\n\
    ifluent(P),instant(I),instant(J).\n\
holdsat(P,J):- initiated(P,I),next(I,J),\n\
    oblfluent(P),instant(I),instant(J).\n\
% all observed events occur\n\
occurred(E,I):- evtype(E,ex),observed(E,I),instant(I).\n\
% a violation occurs for each non-permitted action \n\
occurred(viol(E),I):-\n\
    occurred(E,I),\n\
    evtype(E,ex),\n\
    not holdsat(perm(E),I),\n\
    holdsat(live(X),I),evinst(E,X),\n\
    event(E),instant(I),event(viol(E)).\n\
occurred(viol(E),I):-\n\
    occurred(E,I),\n\
    evtype(E,inst),\n\
    not holdsat(perm(E),I),\n\
    event(E),instant(I),event(viol(E)).\n\
{observed(E,J)}:- evtype(E,ex),instant(J), not final(J).\n\
:- observed(E,J),observed(F,J),instant(J),evtype(E,ex),\n\
   evtype(F,ex), E!=F.\n\
obs(I):- observed(E,I),evtype(E,ex),instant(I).\n\
:- not obs(I), not final(I), instant(I).\n\
% needed until I tidy up some of the constraint generation \n\
true.\
"

#------------------------------------------------------------------------
# command-line parameter processing

def arb(s): return s

parser = argparse.ArgumentParser()
parser.add_argument("-d", "--domain-file", type=arb, help="specify domain file")
parser.add_argument("-i", "--instal-file", type=arb, help="specify instal file")
parser.add_argument("-o", "--output-file", type=arb, help="specify output file")
parser.add_argument("-p1", "--part1-file", type=arb, help="specify part 1 file")
parser.add_argument("-p2", "--part2-file", type=arb, help="specify part 2 file")
# JAP 20121124
parser.add_argument("-t", "--time", type=int, help="specify number of time steps")
args=parser.parse_args()

if args.instal_file:
    f = open(args.instal_file,'r')

if args.part1_file:
    p1 = open(args.part1_file,'r')

if args.part2_file:
    if not(args.part1_file):
        instal_error("Part2 parameter can only be used in conjunction with a part1")
    p2 = open(args.part2_file,'r')
    exit(-1)

if args.output_file:
    instal_output = open(args.output_file,'w')

# set up output defaults
print_part1 = True
print_part2 = True

document = ""

# read inputs

if args.part1_file: document = p1.read(-1); print_part1 = False
if args.part2_file: document = document + p2.read(-1); print_part2 = False
if args.instal_file: document = document + f.read(-1)
else: document = document + sys.stdin.read(-1)

# document should now contain part1 + part2 + part3

yacc.parse(document)

# JAP 20121118: this has to come after the parse of the input file so that
# the type declarations have been processed

typename = r"([A-Z][a-zA-Z0-9_]*)"
literal = r"([a-z][a-zA-Z0-9_]*)"

if args.domain_file:
    f = open(args.domain_file,'r')
    instal_print("%\n% Domain declarations for {institution}\n%".format(**names))
    for l in f.readlines():
        l = l.rstrip() # lose trailing \n
        [t,r] = re.split(": ",l)
        if not(re.search(typename,l)):
            instal_error("ERROR: No type name in {x}".format(x=l))
            exit(-1)
        #check t is declared
        if not(t in types):
            instal_error("ERROR: type not declared in {x}".format(x=l))
            exit(-1)
        t = types[t]
        r = re.split(" ",r)
        for s in r:
            if not(re.search(literal,s)):
                instal_error("ERROR: Unrecognized literal in {x}".format(x=l))
                exit(-1)
            instal_print("{typename}({literalname}).".format(
                typename=t,literalname=s))
    f.close()

# JAP 20121124: this is always output if the -t option is specified.  Not
# sure whether the top of the output file is the right place
if args.time:
    for i in range(0,args.time):
        instal_print("instant({i}).".format(i=i))
    instal_print("start(0).")

# JAP 20121121: what follows attempts to implement the division of code
# generation according to the -p1 and -p2 flags
if print_part1:
    instal_print("%\n% "
                 "-------------------------------"
                 "PART 1"
                 "-------------------------------"
                 "\n%")
    instal_print_standard_prelude()
    instal_print_types()
    instal_print_exevents()
    instal_print_inevents()
    instal_print_vievents()
    instal_print_crevents()
    instal_print_dievents()
    instal_print_dissolve()
    instal_print_inertial_fluents()
    instal_print_noninertial_fluents()
    instal_print_violation_fluents()
    instal_print_obligation_fluents()
else:
    instal_print("%\n% Using part1 from: {f}\n%".format(f=args.part1_file))

if print_part2:
    instal_print("%\n% "
                 "-------------------------------"
                 "PART 2"
                 "-------------------------------"
                 "\n%")
    instal_print_generates()
    instal_print_initiates()
    instal_print_terminates()
    instal_print_noninertials()
else:
    instal_print("%\n% Using part2 from: {f}\n%".format(f=args.part2_file))

instal_print("%\n% "
             "-------------------------------"
             "PART 3"
             "-------------------------------"
             "\n%")
instal_print_initially()

if args.instal_file: f.close()
if args.part1_file: p1.close()
if args.part2_file: p2.close()

instal_print("%\n% End of file\n%")
if args.output_file: instal_output.close()

