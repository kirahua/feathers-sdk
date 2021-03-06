/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package flex2.compiler.mxml;

import flex2.compiler.Source;
import flex2.compiler.as3.AbstractSyntaxTreeUtil;
import flex2.compiler.as3.As3Compiler;
import flex2.compiler.as3.BytecodeEmitter;
import flex2.compiler.mxml.lang.FrameworkDefs;
import flex2.compiler.mxml.lang.StandardDefs;
import flex2.compiler.mxml.gen.StatesGenerator;
import flex2.compiler.mxml.rep.AtEmbed;
import flex2.compiler.mxml.rep.AtResource;
import flex2.compiler.mxml.rep.BindingExpression;
import flex2.compiler.mxml.rep.DocumentInfo;
import flex2.compiler.mxml.rep.Model;
import flex2.compiler.mxml.rep.MovieClip;
import flex2.compiler.mxml.rep.MxmlDocument;
import flex2.compiler.mxml.rep.VariableDeclaration;
import flex2.compiler.mxml.rep.decl.PropertyDeclaration;
import flex2.compiler.mxml.rep.init.EventInitializer;
import flex2.compiler.mxml.rep.init.Initializer;
import flex2.compiler.mxml.rep.init.NamedInitializer;
import flex2.compiler.mxml.rep.init.VisualChildInitializer;
import flex2.compiler.util.NameFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import macromedia.asc.embedding.ConfigVar;
import macromedia.asc.parser.*;
import macromedia.asc.util.ContextStatics;
import macromedia.asc.util.ObjectList;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;

/**
 * This class handles direct AST generation for the second pass full
 * implemenation.
 *
 * @author Paul Reilly
 */
public class ImplementationGenerator extends AbstractGenerator
{
    private static final String ASTERISK = "*";

    // intern all identifier constants
    private static final String __MODULE_FACTORY_INITIALIZED = "__moduleFactoryInitialized".intern();
    private static final String ACCEPT_MEDIA_LIST = "acceptMediaList".intern();
    private static final String ADD_EVENT_LISTENER = "addEventListener".intern();    
    private static final String ADDED_TO_STAGE = "ADDED_TO_STAGE".intern();    
    private static final String ARRAY = "Array".intern();
    private static final String BINDINGS = "bindings".intern();
    private static final String BOOLEAN = "Boolean".intern();
    private static final String CAPITAL_BINDING = "Binding".intern();
    private static final String CHILD_DESCRIPTORS = "childDescriptors".intern();
    private static final String CONCAT = "concat".intern();
    private static final String CONDITION = "condition".intern();
    private static final String CONDITIONS = "conditions".intern();
    private static final String DEFAULT_FACTORY = "defaultFactory".intern();
    private static final String DESIGN_LAYER = "designLayer".intern();
    private static final String DESTINATION = "destination".intern();
    private static final String EFFECTS = "effects".intern();
    private static final String EMBED = "Embed".intern();
    private static final String EVENT_NAME = "event".intern();
    private static final String EVENT_TYPE = "Event".intern();
    private static final String EVENTS = "events".intern();
    private static final String EXECUTE = "execute".intern();
    private static final String FACTORY = "factory".intern();
    private static final String GET_DEFINITION_BY_NAME = "getDefinitionByName".intern();
    private static final String I = "i".intern();
    private static final String ID = "id".intern();
    private static final String IFLEX_MODULE_FACTORY = "IFlexModuleFactory".intern();
    private static final String INIT = "init".intern();
    private static final String INITIALIZE = "initialize".intern();
    private static final String INIT_PROTO_CHAIN_ROOTS = "initProtoChainRoots".intern();
    private static final String INSPECTABLE = "Inspectable".intern();
    private static final String INSTANCE_INDICES = "instanceIndices".intern();
    private static final String IS_TWO_WAY_PRIMARY = "isTwoWayPrimary".intern();
    private static final String IWATCHER_SETUP_UTIL = "IWatcherSetupUtil".intern();
    private static final String LENGTH = "length".intern();
    private static final String LOWERCASE_BINDING = "binding".intern();
    private static final String MODULE_FACTORY = "moduleFactory".intern();
    private static final String OBJECT = "Object".intern();
    private static final String PROXY = "Proxy".intern();
    private static final String PROPERTIES_FACTORY = "propertiesFactory".intern();
    private static final String PROPERTY_NAME = "propertyName".intern();
    private static final String PUSH = "push".intern();
    private static final String REMOVE_EVENT_LISTENER = "removeEventListener".intern();
    private static final String RESOURCE_BUNDLE = "ResourceBundle".intern();
    private static final String RESULT = "result".intern();
    private static final String SELECTOR = "selector".intern();
    private static final String SETUP = "setup".intern();
    private static final String SET_DOCUMENT_DESCRIPTOR = "setDocumentDescriptor".intern();
    private static final String STATIC = "static".intern();
    private static final String STRING = "String".intern();
    private static final String TARGET = "target".intern();
    private static final String TWO_WAY_COUNTERPART = "twoWayCounterpart".intern();
    private static final String TYPE = "type".intern();
    private static final String UINT = "uint".intern();
    private static final String UI_COMPONENT_DESCRIPTOR = "UIComponentDescriptor".intern();
    private static final String UNDEFINED = "undefined".intern();
    private static final String WATCHERS = "watchers".intern();
    private static final String WATCHER_SETUP_UTIL = "watcherSetupUtil".intern();
    private static final String WATCHER_SETUP_UTIL_CLASS = "watcherSetupUtilClass".intern();
    private static final String _BINDINGS = "_bindings".intern();
    private static final String _DOCUMENT = "_document".intern();
    private static final String _DOCUMENT_DESCRIPTOR_ = "_documentDescriptor_".intern();
    private static final String _SOURCE_FUNCTION_RETURN_VALUE = "_sourceFunctionReturnValue".intern();
    private static final String _WATCHERS = "_watchers".intern();
    private static final String _WATCHER_SETUP_UTIL = "_watcherSetupUtil".intern();

    private MxmlDocument mxmlDocument;

    private boolean processComments = false;
    
    ImplementationGenerator(MxmlDocument mxmlDocument, boolean generateDocComments,
            ContextStatics contextStatics, Source source,
            BytecodeEmitter bytecodeEmitter, ObjectList<ConfigVar> defines)
    {
        this(mxmlDocument, generateDocComments, contextStatics, source, bytecodeEmitter, defines, false);
    }
    
    ImplementationGenerator(MxmlDocument mxmlDocument, boolean generateDocComments,
                            ContextStatics contextStatics, Source source,
                            BytecodeEmitter bytecodeEmitter, ObjectList<ConfigVar> defines, boolean processComments)
    {
        super(mxmlDocument.getStandardDefs());

        this.mxmlDocument = mxmlDocument;
        this.generateDocComments = generateDocComments;
        this.processComments = processComments;
        
        context = AbstractSyntaxTreeUtil.generateContext(contextStatics, source,
                                                         bytecodeEmitter, defines);
        nodeFactory = context.getNodeFactory();

        DocCommentNode packageDocComment = null;

        if (generateDocComments)
        {
            packageDocComment = generatePackageDocComment(mxmlDocument.getPackageName(),
                                                          mxmlDocument.getClassName(),
                                                          mxmlDocument.getSourcePath());
        }

        configNamespaces = new HashSet<String>();
        StatementListNode configVars = AbstractSyntaxTreeUtil.parseConfigVars(context, configNamespaces);
        programNode = AbstractSyntaxTreeUtil.generateProgram(context, configVars,
                                                             mxmlDocument.getPackageName(),
                                                             packageDocComment,
                                                             mxmlDocument.getRoot().getXmlLineNumber());
        StatementListNode programStatementList = programNode.statements;

        programStatementList = generateImports(programStatementList);
        programStatementList = generateAtResources(programStatementList);
        programStatementList = generateMetaData(programStatementList, mxmlDocument.getMetadata());

        if(processComments) 
        {
            MetaDataNode classDocComment = null;
            if(mxmlDocument.getComment() != null ) 
            {
                classDocComment = AbstractSyntaxTreeUtil.generateDocComment(nodeFactory, mxmlDocument.getComment().intern());
            } 
            else
            {
                classDocComment = AbstractSyntaxTreeUtil.generateDocComment(nodeFactory, "<description><![CDATA[]]></description>".intern());
            }
            
            if (classDocComment != null)
            {
                programStatementList = nodeFactory.statementList(programStatementList, classDocComment);
            }
        }
        
        ClassDefinitionNode classDefinition = generateClassDefinition();
        programStatementList = nodeFactory.statementList(programStatementList, classDefinition);
        programNode.statements = programStatementList;

        PackageDefinitionNode packageDefinition = nodeFactory.finishPackage(context, null);
        nodeFactory.statementList(programStatementList, packageDefinition);

        // Useful when comparing abstract syntax trees
        //flash.swf.tools.SyntaxTreeDumper.dump(programNode, "/tmp/" + mxmlDocument.getClassName() + "-generated.new.xml");

        As3Compiler.cleanNodeFactory(nodeFactory);
    }

	/**
	 * convenience wrapper for generating non-toplevel descriptor entries
	 */
	public static MemberExpressionNode addDescriptorInitializerFragments(NodeFactory nodeFactory,
                                                                         HashSet<String> configNamespaces,
                                                                         boolean generateDocComments,
                                                                         Model model, Set<String> includePropNames,
                                                                         boolean includeDesignLayer)
	{
		return addDescriptorInitializerFragments(nodeFactory, configNamespaces, generateDocComments,
                                                 model, includePropNames, false, includeDesignLayer);
	}

	/**
	 * @param includePropNames if non-null, this is a set of names of properties to include in the descriptor.
	 *
	 * A filtered set is sometimes needed to conform to the framework API, which requires a handful of properties
	 * (e.g. height, width) be encoded into the top-level descriptor, even though procedural code sets all top-level
	 * ('document') properties.
	 *
	 * Recursive calls to generateDescriptorCode() always pass null for this param, causing all child properties to be
	 * encoded, as required by the framework.
	 *
	 * Note: as with includePropNames, non-property entries are only suppressed (controlled by the propsOnly param to
	 * addDescriptorInitializerFragments being set to true) at the top level of the descriptor.
	 *
	 * Note: _childDescriptor, built from MovieClip.children, is encoded unconditionally at all levels.
	 *
	 * @param propsOnly if true, event, effect and style entries are suppressed. This is a top- vs. nontop-level
	 * constraint, like includePropNames.
	 */
	private static MemberExpressionNode addDescriptorInitializerFragments(NodeFactory nodeFactory,
                                                                          HashSet<String> configNamespaces,
                                                                          boolean generateDocComments,
                                                                          Model model, Set includePropNames,
                                                                          boolean propsOnly, boolean includeDesignLayer)
	{
		model.setDescribed(true);

        LiteralStringNode mxCoreLiteralString = nodeFactory.literalString(model.getStandardDefs().getCorePackage(), false);
        QualifiedIdentifierNode uiComponentDescriptorQualifiedIdentifier =
            nodeFactory.qualifiedIdentifier(mxCoreLiteralString, UI_COMPONENT_DESCRIPTOR);

        IdentifierNode typeIdentifier = nodeFactory.identifier(TYPE, false);
        String modelTypeName = model.getType().getName();
        String packageName = NameFormatter.retrievePackageName(modelTypeName);
        String className = NameFormatter.retrieveClassName(modelTypeName).intern();
        Node modelTypeIdentifier;

        int position = AbstractSyntaxTreeUtil.lineNumberToPosition(nodeFactory, model.getXmlLineNumber());
        
        if (packageName.equals(""))
        {
            modelTypeIdentifier = nodeFactory.identifier(className, false);
        }
        else
        {
            LiteralStringNode packageNameLiteralString = nodeFactory.literalString(packageName);
            modelTypeIdentifier = nodeFactory.qualifiedIdentifier(packageNameLiteralString, className);
        }

        GetExpressionNode typeGetExpression = nodeFactory.getExpression(modelTypeIdentifier, position);
        MemberExpressionNode typeMemberExpression = nodeFactory.memberExpression(null, typeGetExpression);
        LiteralFieldNode typeLiteralField = nodeFactory.literalField(typeIdentifier, typeMemberExpression);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, typeLiteralField);

		//	id?
		if (model.isDeclared())
		{
            IdentifierNode idIdentifier = nodeFactory.identifier(ID, false);
            LiteralStringNode idLiteralString = nodeFactory.literalString(model.getId());
            LiteralFieldNode idLiteralField = nodeFactory.literalField(idIdentifier, idLiteralString);
            argumentList = nodeFactory.argumentList(argumentList, idLiteralField);
		}
        
		//	events?
		if (!propsOnly)
        {
			argumentList = addDescriptorEvents(nodeFactory, argumentList, model);
        }

		//	descriptor properties are Model.properties + synthetic property 'childDescriptors' from MovieClip.children
		argumentList = addDescriptorProperties(nodeFactory, configNamespaces, generateDocComments, argumentList,
                                               model, includePropNames, includeDesignLayer);

        LiteralObjectNode literalObject = nodeFactory.literalObject(argumentList);
        ArgumentListNode uiComponentDescriptorArgumentList = nodeFactory.argumentList(null, literalObject);
        CallExpressionNode uiComponentDescriptorCallExpression =
            (CallExpressionNode) nodeFactory.callExpression(uiComponentDescriptorQualifiedIdentifier,
                                                            uiComponentDescriptorArgumentList);
        uiComponentDescriptorCallExpression.is_new = true;
        uiComponentDescriptorCallExpression.setRValue(false);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(null, uiComponentDescriptorCallExpression);

        return memberExpression;
	}

    /**
     *
     */
    private static ArgumentListNode addDescriptorProperties(NodeFactory nodeFactory, HashSet<String> configNamespaces,
                                                            boolean generateDocComments, ArgumentListNode argumentList,
                                                            Model model, final Set includePropNames, boolean includeDesignLayer)
    {
        //  ordinary properties
        Iterator propIter = includePropNames == null ?
            model.getPropertyInitializerIterator(false) :
            new FilterIterator(model.getPropertyInitializerIterator(false), new Predicate() {
                    public boolean evaluate(Object obj)
                    {
                        return includePropNames.contains(((NamedInitializer) obj).getName());
                    }
                });

        //  visual children
        Iterator vcIter = (model instanceof MovieClip && ((MovieClip)model).hasChildren()) ?
            ((MovieClip)model).children().iterator() :
            Collections.EMPTY_LIST.iterator();

        // designLayer ?
        Boolean hasDesignLayer = (includeDesignLayer && (model.layerParent != null) &&
                                  model.getType().isAssignableTo(model.getStandardDefs().INTERFACE_IVISUALELEMENT));
            
        if (propIter.hasNext() || vcIter.hasNext() || hasDesignLayer)
        {
            IdentifierNode propertiesFactoryIdentifier = nodeFactory.identifier(PROPERTIES_FACTORY, false);
            MemberExpressionNode objectMemberExpression =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, OBJECT, false);
            TypeExpressionNode typeExpression = nodeFactory.typeExpression(objectMemberExpression,
                                                                           true, false, -1);
            FunctionSignatureNode functionSignature = nodeFactory.functionSignature(null, typeExpression);
            ArgumentListNode propertiesFactoryArgumentList = null;

            // properties
            while (propIter.hasNext())
            {
                NamedInitializer init = (NamedInitializer) propIter.next();
                if (!init.isStateSpecific())
                {
                    IdentifierNode propertyIdentifier = nodeFactory.identifier(init.getName());
                    Node valueExpr = init.generateValueExpr(nodeFactory, configNamespaces, generateDocComments);
                    LiteralFieldNode propertyLiteralField = nodeFactory.literalField(propertyIdentifier,
                                                                                 valueExpr);
                    propertiesFactoryArgumentList = nodeFactory.argumentList(propertiesFactoryArgumentList,
                                                                         propertyLiteralField);
                }
            }

            // designLayer
            if (hasDesignLayer)
            {
            	if (model.getType().isAssignableTo(model.getStandardDefs().INTERFACE_IVISUALELEMENT))
                {
                    IdentifierNode layerPropertyIdentifier = nodeFactory.identifier(DESIGN_LAYER, false);
                    MemberExpressionNode memberExpression =
                        AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, model.layerParent.getId(), true);
                	LiteralFieldNode layerLiteralField = nodeFactory.literalField(layerPropertyIdentifier, memberExpression);
                	propertiesFactoryArgumentList = nodeFactory.argumentList(propertiesFactoryArgumentList,
                			layerLiteralField);
                } 
            }
            
            // visual children
            if (vcIter.hasNext())
            {
                IdentifierNode childDescriptorsIdentifier = nodeFactory.identifier(CHILD_DESCRIPTORS, false);
                ArgumentListNode childDescriptorsArgumentList = null;

                while (vcIter.hasNext())
                {
                    VisualChildInitializer init = (VisualChildInitializer) vcIter.next();
                    Model child = (MovieClip)init.getValue();
                    if (child.isDescriptorInit()) 
                    {
                        MemberExpressionNode memberExpression =
                            addDescriptorInitializerFragments(nodeFactory, configNamespaces, generateDocComments,
                                                              (MovieClip) init.getValue(), null, true);
                        childDescriptorsArgumentList = nodeFactory.argumentList(childDescriptorsArgumentList,
                                                                            memberExpression);
                    }
                }

                LiteralArrayNode literalArray = nodeFactory.literalArray(childDescriptorsArgumentList);
                LiteralFieldNode childDescriptorsLiteralField =
                    nodeFactory.literalField(childDescriptorsIdentifier, literalArray);
                propertiesFactoryArgumentList = nodeFactory.argumentList(propertiesFactoryArgumentList,
                                                                         childDescriptorsLiteralField);
            }

            LiteralObjectNode literalObject = nodeFactory.literalObject(propertiesFactoryArgumentList);
            ListNode list = nodeFactory.list(null, literalObject);
            ReturnStatementNode returnStatement = nodeFactory.returnStatement(list);
            StatementListNode body = nodeFactory.statementList(null, returnStatement);
            FunctionCommonNode functionCommon = nodeFactory.functionCommon(nodeFactory.getContext(),
                                                                           null, functionSignature, body);
            functionCommon.setUserDefinedBody(true);
            LiteralFieldNode propertiesFactoryLiteralField =
                nodeFactory.literalField(propertiesFactoryIdentifier, functionCommon);
            argumentList = nodeFactory.argumentList(argumentList, propertiesFactoryLiteralField);
        }

        return argumentList;
    }

	/**
	 *
	 */
	private static ArgumentListNode addDescriptorEvents(NodeFactory nodeFactory,
                                                        ArgumentListNode argumentList, Model model)
	{
		Iterator eventIter = model.getEventInitializerIterator();

		if (eventIter.hasNext())
		{
            IdentifierNode eventsIdentifier = nodeFactory.identifier(EVENTS, false);
            ArgumentListNode eventsArgumentList = null;

			while (eventIter.hasNext())
			{
				EventInitializer init = (EventInitializer) eventIter.next();
				IdentifierNode identifier = nodeFactory.identifier(init.getName());
                LiteralStringNode literalString = nodeFactory.literalString(init.getValueExpr());
                LiteralFieldNode literalField = nodeFactory.literalField(identifier, literalString);
                eventsArgumentList = nodeFactory.argumentList(eventsArgumentList, literalField);
			}

            LiteralObjectNode eventsLiteralObject = nodeFactory.literalObject(eventsArgumentList);
            LiteralFieldNode eventsLiteralField = nodeFactory.literalField(eventsIdentifier, eventsLiteralObject);
            argumentList = nodeFactory.argumentList(argumentList, eventsLiteralField);
		}

        return argumentList;
	}

    private Set<String> createInterfaceNames()
    {
        Set<String> result = new TreeSet<String>();
        Iterator<DocumentInfo.NameInfo> iterator = mxmlDocument.getInterfaceNames().iterator();

        while (iterator.hasNext())
        {
            result.add(iterator.next().getName());
        }

        return result;
    }

    protected StatementListNode generateAtResources(StatementListNode programStatementList)
    {
        StatementListNode result = programStatementList;
        Iterator<AtResource> iterator = mxmlDocument.getAtResources().iterator();

        while (iterator.hasNext())
        {
            //[ResourceBundle("$atResource.bundle")]
            String bundle = iterator.next().getBundle();
            MetaDataNode metaData =
                AbstractSyntaxTreeUtil.generateMetaData(nodeFactory, RESOURCE_BUNDLE, bundle);
            result = nodeFactory.statementList(result, metaData);
        }

        return result;
    }

    private ExpressionStatementNode generateBinding(BindingExpression bindingExpression)
    {
        // result[${bindingExpression.id}] = new mx.binding.Binding(this, ...
        MemberExpressionNode base = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        LiteralNumberNode literalNumber = nodeFactory.literalNumber(bindingExpression.getId());
        ArgumentListNode literalNumberArgumentList = nodeFactory.argumentList(null, literalNumber);

        QualifiedIdentifierNode qualifiedIdentifier =
            AbstractSyntaxTreeUtil.generateQualifiedIdentifier(nodeFactory, standardDefs.getBindingPackage(), CAPITAL_BINDING, false);
        ThisExpressionNode thisExpression = nodeFactory.thisExpression(-1);
        ArgumentListNode callArgumentList = nodeFactory.argumentList(null, thisExpression);

        if (bindingExpression.isSourcePublicProperty() && !bindingExpression.getDestinationTypeName().equals(ARRAY))
        {
            int position = AbstractSyntaxTreeUtil.lineNumberToPosition(nodeFactory, bindingExpression.getXmlLineNumber());
            LiteralNullNode literalNull = nodeFactory.literalNull(position);
            callArgumentList = nodeFactory.argumentList(callArgumentList, literalNull);
        }
        else
        {
            FunctionCommonNode sourceFunctionCommon = generateSourceFunction(bindingExpression);
            callArgumentList = nodeFactory.argumentList(callArgumentList, sourceFunctionCommon);
        }

        if (bindingExpression.isSimpleChain() && !bindingExpression.isDestinationNonPublicProperty() )
        {
            LiteralNullNode literalNull = nodeFactory.literalNull(-1);
            callArgumentList = nodeFactory.argumentList(callArgumentList, literalNull);
        }
        else
        {
            FunctionCommonNode destinationFunctionCommon = generateDestinationFunction(bindingExpression);
            callArgumentList = nodeFactory.argumentList(callArgumentList, destinationFunctionCommon);
        }

        LiteralStringNode destLiteralString = nodeFactory.literalString(bindingExpression.getDestinationPath(false));
        callArgumentList = nodeFactory.argumentList(callArgumentList, destLiteralString);

        if (bindingExpression.isSourcePublicProperty())
        {
            LiteralStringNode srcLiteralString = nodeFactory.literalString(bindingExpression.getSourceAsProperty());
            callArgumentList = nodeFactory.argumentList(callArgumentList, srcLiteralString);
        }

        CallExpressionNode callExpression = (CallExpressionNode) nodeFactory.callExpression(qualifiedIdentifier,
                                                                                                callArgumentList);
        callExpression.is_new = true;
        callExpression.setRValue(false);
        MemberExpressionNode bindingMemberExpression = nodeFactory.memberExpression(null, callExpression);
        ArgumentListNode setArgumentList = nodeFactory.argumentList(null, bindingMemberExpression);
        SetExpressionNode selector = nodeFactory.setExpression(literalNumberArgumentList,
                                                               setArgumentList, false);
        selector.setMode(Tokens.LEFTBRACKET_TOKEN);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(base, selector);
        ListNode list = nodeFactory.list(null, memberExpression);
        return nodeFactory.expressionStatement(list);
    }

    private Node generateBindingsForLoop()
    {
        // for (var i:uint = 0; i < bindings.length; i++)
        // Binding(bindings[i]).execute();

        MemberExpressionNode iMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, I, false);
        MemberExpressionNode bindingsMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, BINDINGS, false);

        IdentifierNode lengthIdentifier = nodeFactory.identifier(LENGTH, false);
        GetExpressionNode lengthGetExpression = nodeFactory.getExpression(lengthIdentifier);
        lengthGetExpression.setMode(Tokens.DOT_TOKEN);
        MemberExpressionNode lengthMemberExpression = nodeFactory.memberExpression(bindingsMemberExpression,
                                                                                   lengthGetExpression);

        BinaryExpressionNode binaryExpression = nodeFactory.binaryExpression(Tokens.LESSTHAN_TOKEN,
                                                                             iMemberExpression,
                                                                             lengthMemberExpression);
        ListNode test = nodeFactory.list(null, binaryExpression);

        IdentifierNode iIdentifier = nodeFactory.identifier(I, false);
        IncrementNode incrementNode = nodeFactory.increment(Tokens.PLUSPLUS_TOKEN, iIdentifier, true);
        MemberExpressionNode iteratorMemberExpression = nodeFactory.memberExpression(null,
                                                                                     incrementNode);
        ListNode increment = nodeFactory.list(null, iteratorMemberExpression);

        IdentifierNode bindingIdentifier = nodeFactory.identifier(CAPITAL_BINDING, false);
        bindingsMemberExpression = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory,
                                                                                 BINDINGS, false);
        iMemberExpression = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, I, false);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, iMemberExpression);
        GetExpressionNode getExpression = nodeFactory.getExpression(argumentList);
        getExpression.setMode(Tokens.LEFTBRACKET_TOKEN);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(bindingsMemberExpression,
                                                                             getExpression);
        ArgumentListNode callArgumentList = nodeFactory.argumentList(null, memberExpression);
        CallExpressionNode bindingCallExpression =
            (CallExpressionNode) nodeFactory.callExpression(bindingIdentifier, callArgumentList);
        bindingCallExpression.setRValue(false);
        MemberExpressionNode base = nodeFactory.memberExpression(null, bindingCallExpression);
        IdentifierNode executeIdentifier = nodeFactory.identifier(EXECUTE, false);
        CallExpressionNode executeCallExpression =
            (CallExpressionNode) nodeFactory.callExpression(executeIdentifier, null);
        executeCallExpression.setRValue(false);
        MemberExpressionNode MemberExpression = nodeFactory.memberExpression(base, executeCallExpression);
        ListNode list = nodeFactory.list(null, MemberExpression);
        ExpressionStatementNode expressionStatement = nodeFactory.expressionStatement(list);
        StatementListNode statement = nodeFactory.statementList(null, expressionStatement);

        return nodeFactory.forStatement(null, test, increment, statement);
    }

    private StatementListNode generateBindingManagementVars(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        Iterator<VariableDeclaration> iterator = MxmlDocument.getBindingManagementVars().iterator();

        while (iterator.hasNext())
        {
            VariableDeclaration variableDeclaration = iterator.next();
            if (!mxmlDocument.superHasPublicProperty(variableDeclaration.getName()))
            {
                if (generateDocComments)
                {
                    DocCommentNode docComment =
                        AbstractSyntaxTreeUtil.generatePrivateDocComment(nodeFactory);
                    result = nodeFactory.statementList(result, docComment);
                }

                //$var.namespace var $var.name : $var.type = $var.initializer;
                String initializerString = variableDeclaration.getInitializer();
                Node initializerNode = null;

                if (initializerString.equals("[]"))
                {
                    initializerNode = nodeFactory.literalArray(null);
                }
                else if (initializerString.equals("{}"))
                {
                    initializerNode = nodeFactory.literalObject(null);
                }
                else
                {
                    assert false : initializerString;
                }

                String variableName = variableDeclaration.getName();
                QualifiedIdentifierNode qualifiedIdentifier =
                    AbstractSyntaxTreeUtil.generateMxInternalQualifiedIdentifier(nodeFactory,
                                                                                 variableName,
                                                                                 false);
                VariableDefinitionNode variableDefinition =
                    AbstractSyntaxTreeUtil.generateVariable(nodeFactory,
                                                            generateMxInternalAttribute(),
                                                            qualifiedIdentifier,
                                                            variableDeclaration.getType(),
                                                            false,
                                                            initializerNode);

                result = nodeFactory.statementList(result, variableDefinition);
            }
        }

        return result;
    }

    private ExpressionStatementNode generateBindingsAssignment()
    {
        MemberExpressionNode mxInternalGetterSelector =
            AbstractSyntaxTreeUtil.generateResolvedGetterSelector(nodeFactory, standardDefs.getCorePackage(), MX_INTERNAL);
        QualifiedIdentifierNode qualifiedIdentifier =
            AbstractSyntaxTreeUtil.generateQualifiedIdentifier(nodeFactory, mxInternalGetterSelector,
                                                               _BINDINGS, false);
        MemberExpressionNode rvalueBase = generateMxInternalGetterSelector(_BINDINGS, false);
        IdentifierNode concatIdentifier = nodeFactory.identifier(CONCAT, false);
        MemberExpressionNode bindingsMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, BINDINGS, false);
        ArgumentListNode concatArgumentList = nodeFactory.argumentList(null, bindingsMemberExpression);
        CallExpressionNode callExpression =
            (CallExpressionNode) nodeFactory.callExpression(concatIdentifier, concatArgumentList);
        callExpression.setRValue(false);
        MemberExpressionNode argument = nodeFactory.memberExpression(rvalueBase, callExpression);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, argument);
        SetExpressionNode setExpression = nodeFactory.setExpression(qualifiedIdentifier, argumentList, false);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(null, setExpression);
        ListNode list = nodeFactory.list(null, memberExpression);
        return nodeFactory.expressionStatement(list);
    }

    /**
     * Equivalent from ClassDefLib.vm:
     * <PRE>
     * private function ${convertedClassName}_bindingExprs():void
     * {
     *     ${doc.getAllBindingNamespaceDeclarations()}
     *     #foreach ($bindingExpression in $doc.bindingExpressions)
     *     #if (!$bindingExpression.destination)
     *     #embedText("$bindingExpression.destinationProperty = $bindingExpression.sourceExpression;" $bindingExpression.xmlLineNumber)
     *     #end
     *     #end
     * }
     * </PRE>
     */
    private StatementListNode generateBindingExprsFunction(StatementListNode statementList) 	 
    { 	 
        StatementListNode result = statementList; 	 
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(null, null); 	 
        functionSignature.void_anno = true; 	 
        VariableDefinitionNode variableDefinition = 	 
            AbstractSyntaxTreeUtil.generateVariable(nodeFactory, DESTINATION, false); 	 
        StatementListNode functionStatementList = nodeFactory.statementList(null, 	 
                                                                            variableDefinition); 	 
	  	 
        // ${doc.getAllBindingNamespaces()} 	 
        Map<Integer, String> allNs = mxmlDocument.getAllBindingNamespaces(); 	 
        if (allNs.size() > 0) 	 
        { 	 
            functionStatementList = BindingExpression.generateNamespaceDeclarations(allNs, context, 	 
                                                                                    functionStatementList); 	 
        } 	 
	  	 
        for (BindingExpression bindingExpression : mxmlDocument.getBindingExpressions()) 	 
        { 	 
            if (bindingExpression.getDestination() == null) 	 
            { 	 
                //$bindingExpression.destinationProperty = $bindingExpression.sourceExpression; 	 
                String text = (bindingExpression.getDestinationProperty() + " = " + 	 
                               bindingExpression.getSourceExpression()); 	 
                int xmlLineNumber = bindingExpression.getXmlLineNumber(); 	 
                List<Node> nodeList =
                    AbstractSyntaxTreeUtil.parseExpression(context, configNamespaces, text,
                                                           xmlLineNumber, generateDocComments);
	  	 
                if (!nodeList.isEmpty()) 	 
                { 	 
                    functionStatementList = nodeFactory.statementList(functionStatementList, nodeList.get(0));
                } 	 
            } 	 
        } 	 
	  	 
        FunctionCommonNode functionCommon = nodeFactory.functionCommon(context, null, functionSignature, 	 
                                                                       functionStatementList); 	 
        functionCommon.setUserDefinedBody(true); 	 
	  	 
        AttributeListNode attributeList = AbstractSyntaxTreeUtil.generatePrivateAttribute(nodeFactory); 	 
        IdentifierNode bindingsSetupIdentifier = nodeFactory.identifier(mxmlDocument.getConvertedClassName() + 	 
                                                                        "_bindingExprs"); 	 
        FunctionNameNode functionName = nodeFactory.functionName(Tokens.EMPTY_TOKEN, bindingsSetupIdentifier); 	 
	  	 
        FunctionDefinitionNode functionDefinition = nodeFactory.functionDefinition(context, attributeList, 	 
                                                                                   functionName, functionCommon); 	 
        return nodeFactory.statementList(result, functionDefinition); 	 
    }

    private StatementListNode generateBindingsSetup(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        result = generateBindingsSetupFunction(result);

        if (mxmlDocument.hasBindingTags()) 	 
        { 	 
            // Output a source to destination assignment expression
            // for each Binding tag, so that ASC can detect coercion
            // errors.  This function is never called, so we could
            // potentially remove it before code generation.
            result = generateBindingExprsFunction(result);
        }

        result = generateSetWatcherSetupUtilFunction(result);

        AttributeListNode attributeList =
            AbstractSyntaxTreeUtil.generatePrivateStaticAttribute(nodeFactory);
        QualifiedIdentifierNode qualifiedIdentifier =
            AbstractSyntaxTreeUtil.generateMxInternalQualifiedIdentifier(nodeFactory,
                                                                         _WATCHER_SETUP_UTIL,
                                                                         false);
        VariableDefinitionNode variableDefinition =
            AbstractSyntaxTreeUtil.generateVariable(nodeFactory, attributeList,
                                                    qualifiedIdentifier,
                                                    IWATCHER_SETUP_UTIL, false, null);

        return nodeFactory.statementList(result, variableDefinition);
    }

    private StatementListNode generateBindingsSetupFunction(StatementListNode statementList)
    {
        StatementListNode result = statementList;
        MemberExpressionNode arrayMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, ARRAY, false);
        TypeExpressionNode returnType = nodeFactory.typeExpression(arrayMemberExpression, true, false, -1);
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(null, returnType);

        Node resultVariableDefinition = generateResultVariable();
        StatementListNode functionStatementList = nodeFactory.statementList(null, resultVariableDefinition);

        Iterator<BindingExpression> iterator = mxmlDocument.getBindingExpressions().iterator();

        while (iterator.hasNext())
        {
            BindingExpression bindingExpression = iterator.next();

            {
                ExpressionStatementNode expressionStatement = generateBinding(bindingExpression);
                functionStatementList = nodeFactory.statementList(functionStatementList, expressionStatement);
            }

            if (bindingExpression.getTwoWayCounterpart() != null)
            {
                if (bindingExpression.isTwoWayPrimary())
                {
                    // result[${bindingExpression.id}].isTwoWayPrimary = true;
                    ExpressionStatementNode expressionStatement =
                        generateIsTwoWayPrimaryAssignment(bindingExpression.getId());
                    functionStatementList = nodeFactory.statementList(functionStatementList, expressionStatement);
                }

                {
                    // result[${bindingExpression.id}].twoWayCounterpart = result[${bindingExpression.twoWayCounterpart.id}];
                    ExpressionStatementNode expressionStatement =
                        generateTwoWayCounterpartAssignment(bindingExpression.getId(),
                                                            bindingExpression.getTwoWayCounterpart().getId());
                    functionStatementList = nodeFactory.statementList(functionStatementList, expressionStatement);
                }

                if (bindingExpression.getTwoWayCounterpart().isTwoWayPrimary())
                {
                    // result[${bindingExpression.twoWayCounterpart.id}].isTwoWayPrimary = true;
                    ExpressionStatementNode expressionStatement =
                        generateIsTwoWayPrimaryAssignment(bindingExpression.getTwoWayCounterpart().getId());
                    functionStatementList = nodeFactory.statementList(functionStatementList, expressionStatement);
                }

                {
                    //result[${bindingExpression.twoWayCounterpart.id}].twoWayCounterpart = result[${bindingExpression.id}];
                    ExpressionStatementNode expressionStatement =
                        generateTwoWayCounterpartAssignment(bindingExpression.getTwoWayCounterpart().getId(),
                                                            bindingExpression.getId());
                    functionStatementList = nodeFactory.statementList(functionStatementList, expressionStatement);
                }
            }
        }

        MemberExpressionNode memberExpression = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory,
                                                                                              RESULT, false);
        ListNode list = nodeFactory.list(null, memberExpression);
        ReturnStatementNode returnStatement = nodeFactory.returnStatement(list);
        functionStatementList = nodeFactory.statementList(functionStatementList, returnStatement);

        FunctionCommonNode functionCommon = nodeFactory.functionCommon(context, null, functionSignature,
                                                                       functionStatementList);
        functionCommon.setUserDefinedBody(true);

        AttributeListNode attributeList = AbstractSyntaxTreeUtil.generatePrivateAttribute(nodeFactory);
        IdentifierNode bindingsSetupIdentifier = nodeFactory.identifier(mxmlDocument.getConvertedClassName() +
                                                                        "_bindingsSetup");
        FunctionNameNode functionName = nodeFactory.functionName(Tokens.EMPTY_TOKEN, bindingsSetupIdentifier);

        FunctionDefinitionNode functionDefinition = nodeFactory.functionDefinition(context, attributeList,
                                                                                   functionName, functionCommon);
        return nodeFactory.statementList(result, functionDefinition);
    }

    private Node generateBindingVariable()
    {
        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, LOWERCASE_BINDING);
        MemberExpressionNode memberExpression = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, CAPITAL_BINDING, false);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, typeExpression);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier, null);
        ListNode list = nodeFactory.list(null, variableBinding);
        return nodeFactory.variableDefinition(null, kind, list);
    }

    private Node generateBindingsVariable()
    {
        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, BINDINGS);
        MemberExpressionNode memberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, ARRAY, false);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, typeExpression);

        IdentifierNode bindingsSetupIdentifier = nodeFactory.identifier(mxmlDocument.getConvertedClassName() +
                                                                        "_bindingsSetup");
        CallExpressionNode callExpression = (CallExpressionNode) nodeFactory.callExpression(bindingsSetupIdentifier,
                                                                                            null);
        callExpression.setRValue(false);
        MemberExpressionNode initializer = nodeFactory.memberExpression(null, callExpression);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier, initializer);
        ListNode list = nodeFactory.list(null, variableBinding);
        return nodeFactory.variableDefinition(null, kind, list);
    }

    private ClassDefinitionNode generateClassDefinition()
    {
        StatementListNode statementList = null;
        
        statementList = generateInstanceVariables(statementList);
        statementList = generateTypeImportDummies(statementList);

        String className = mxmlDocument.getClassName();

        if (mxmlDocument.getIsIUIComponent())
        {
            if (mxmlDocument.getDescribeVisualChildren() && 
                mxmlDocument.getIsContainer())
            {
                // Container document descriptor
                MemberExpressionNode memberExpression =
                    AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, standardDefs.getCorePackage(),
                                                                  UI_COMPONENT_DESCRIPTOR, false);
                TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
                MemberExpressionNode initializer = getDescriptorInitializerFragments(nodeFactory, configNamespaces,
                                                                                     generateDocComments, mxmlDocument.getRoot());
                Node documentDescriptorVariableDefinition =
                    AbstractSyntaxTreeUtil.generatePrivateVariable(nodeFactory, typeExpression,
                                                                   _DOCUMENT_DESCRIPTOR_, initializer);
                statementList = nodeFactory.statementList(statementList, documentDescriptorVariableDefinition);
            }

            StatementListNode constructorStatementList = null;
            if (mxmlDocument.getIsContainer() || mxmlDocument.getIsVisualElementContainer())
            {
                ExpressionStatementNode expressionStatement = generateDocumentAssignment();
                constructorStatementList = nodeFactory.statementList(constructorStatementList,
                                                                     expressionStatement);
            }
            
            constructorStatementList = generateBindingInitializers(constructorStatementList);
            constructorStatementList = generateComponentInitializers(constructorStatementList);
            constructorStatementList = generateStatesInitializers(constructorStatementList);
            constructorStatementList = generateInitialBindingExecutions(constructorStatementList);
           	
            statementList = generateConstructor(statementList, generateDocComments,
                                                className, null, 
                                                constructorStatementList);

           	if (mxmlDocument.getIsIFlexModule())
           	{
           		statementList = nodeFactory.statementList(statementList, 
           										generateModuleFactoryInitializedVariable());
           		
           		if (generateDocComments)
           		{
           		    DocCommentNode docComment = AbstractSyntaxTreeUtil.generatePrivateDocComment(nodeFactory);
           		    statementList = nodeFactory.statementList(statementList, docComment);
           		}
           	}

           	if (generateDocComments)
            {
                DocCommentNode docComment =
                    AbstractSyntaxTreeUtil.generatePrivateDocComment(nodeFactory);
                statementList = nodeFactory.statementList(statementList, docComment);
            }
            
            statementList = nodeFactory.statementList(statementList, generateInitializeFunction());
        }
        else
        {
            StatementListNode constructorStatementList = null;

            constructorStatementList = generateBindingInitializers(constructorStatementList);
            constructorStatementList = generateStatesInitializers(constructorStatementList);
            constructorStatementList = generateComponentInitializers(constructorStatementList);
            constructorStatementList = generateInitialBindingExecutions(constructorStatementList);

            statementList = generateConstructor(statementList, generateDocComments,
                                                className, null, 
                                                constructorStatementList);
        }
        
        
        if (mxmlDocument.getHasStagePropertyInitializers())
        {
            statementList = generateAddedToStageHandlerFunction(statementList);
        }
        
        statementList = generateScripts(statementList, mxmlDocument.getScripts());
        statementList = generateInitializerSupportDefs(statementList);
        statementList = generateEmbeds(statementList);

        if (mxmlDocument.getBindingExpressions().size() > 0)
        {
            statementList = generateBindingManagementVars(statementList);
        }
        
        return AbstractSyntaxTreeUtil.generateClassDefinition(context, className,
                                                              mxmlDocument.getSuperClassName(),
                                                              createInterfaceNames(), statementList);
    }
    
	/**
     * Generate code to create the listener function for the Event.ADDED_TO_STAGE method.
     * 
     * <pre>
     *  private function _MyFlex4_addedToStageHandler(event:Event):void
     *  {
     *       removeEventListener(Event.ADDED_TO_STAGE, _MyFlex4_addedToStageHandler);
     *
     *       // stage properties
     *       ...
     *  }
     *
     * </pre>
     * 
     * @param statementList
     * @return
     */
    private StatementListNode generateAddedToStageHandlerFunction(StatementListNode statementList)
    {
        StatementListNode result = statementList;
        
        // function signature
        ParameterNode parameter = AbstractSyntaxTreeUtil.generateParameter(nodeFactory,
                                        EVENT_NAME,
                                        EVENT_TYPE, false);
        ParameterListNode parameterList = nodeFactory.parameterList(null, parameter);
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(parameterList, null);
        functionSignature.void_anno = true;

        // function body
        StatementListNode bodyList = null;

        bodyList = generateEventListenerCall(bodyList, false);
        bodyList = generatePropertyInitializers(bodyList, true);
        
        FunctionCommonNode functionCommon = nodeFactory.functionCommon(context, null, functionSignature, bodyList);
        functionCommon.setUserDefinedBody(true);

        AttributeListNode attributeList = AbstractSyntaxTreeUtil.generatePrivateAttribute(nodeFactory);
        String funcName = getAddedToStageHandlerFunctionName();
        IdentifierNode identifier = nodeFactory.identifier(funcName, true);
        FunctionNameNode functionName = nodeFactory.functionName(Tokens.EMPTY_TOKEN, identifier);

        FunctionDefinitionNode functionDefinition = nodeFactory.functionDefinition(context, attributeList, functionName, functionCommon);
        result = nodeFactory.statementList(result, functionDefinition);
        return result;
    }
    
    /**
     * @return name of the addedToStageHandler function. 
     */
    private String getAddedToStageHandlerFunctionName()
    {
        return "_" + mxmlDocument.getClassName() + "_addedToStageHandler";
    }

    private StatementListNode generateComponentInitializers(StatementListNode statementList)
    {
        StatementListNode result = statementList;
        
        result = generateDesignLayerInitializers(result);
        result = generatePropertyInitializers(result, false);
        result = generateEventListenerCall(result, true);
        result = generateEventInitializers(result);

        return result;
    }
    
    /**
     * Generate code to call either "addEventListener(Event.ADDED_TO_STAGE, _{$doc.className}_addedToStageHandler);"
     * or "removeEventListener(Event.ADDED_TO_STAGE, _{$doc.className}_addedToStageHandler);" depending of the 
     * value of <code>addListener</code>
     * 
     * @param statementList
     * @param addListener - if true, generated "addEventListener", otherwise generate "removeEventListener".
     * @return
     */
    private StatementListNode generateEventListenerCall(StatementListNode statementList, boolean addListener)
    {
        StatementListNode result = statementList;

        if (mxmlDocument.getHasStagePropertyInitializers())
        {
            IdentifierNode addedToStageIdentifierNode = nodeFactory.identifier(ADDED_TO_STAGE, false);
            GetExpressionNode addedToStageGetExpression = nodeFactory.getExpression(addedToStageIdentifierNode);
            IdentifierNode eventIdentifierNode = nodeFactory.identifier(EVENT_TYPE, false);
            GetExpressionNode eventGetExpression = nodeFactory.getExpression(eventIdentifierNode);
            MemberExpressionNode eventMemberExpression = nodeFactory.memberExpression(null, eventGetExpression);
            MemberExpressionNode eventAddedToStageMemberExpression =
                nodeFactory.memberExpression(eventMemberExpression, addedToStageGetExpression);
            // create second arg for function handler
            String addedToStageFuncName = getAddedToStageHandlerFunctionName();
            MemberExpressionNode addedToStageFuncNode = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, 
                                                        addedToStageFuncName, true);

            ArgumentListNode argumentList = nodeFactory.argumentList(null, eventAddedToStageMemberExpression);
            nodeFactory.argumentList(argumentList, addedToStageFuncNode);
            
            IdentifierNode addEventListenerIdentifier = nodeFactory.identifier(addListener ? ADD_EVENT_LISTENER : 
                                                                                             REMOVE_EVENT_LISTENER, 
                                                                               false);
            CallExpressionNode callExpression = (CallExpressionNode)nodeFactory.callExpression(addEventListenerIdentifier, 
                                                                                               argumentList);
            callExpression.setRValue(false);
            MemberExpressionNode memberExpression = nodeFactory.memberExpression(null, callExpression);
            ListNode list = nodeFactory.list(null, memberExpression);
            ExpressionStatementNode expressionStatement = nodeFactory.expressionStatement(list);
            result = nodeFactory.statementList(result, expressionStatement);
        }

        return result;
    }

    
    private VariableDefinitionNode generateVariable(String name, String type)
    {
        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = generateMxInternalQualifiedIdentifier(name, false);
        MemberExpressionNode memberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, type, false);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, typeExpression);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier, null);
        ListNode list = nodeFactory.list(null, variableBinding);
        return (VariableDefinitionNode) nodeFactory.variableDefinition(null, kind, list);
    }

    private StatementListNode generateBindingInitializers(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        if (mxmlDocument.getBindingExpressions().size() > 0)
        {
            Node bindingsVariableDefinition = generateBindingsVariable();
            result = nodeFactory.statementList(result, bindingsVariableDefinition);
            Node watchersVariableDefinition = generateWatchersVariable();
            result = nodeFactory.statementList(result, watchersVariableDefinition);
            Node targetVariableDefinition = generateTargetVariable();
            result = nodeFactory.statementList(result, targetVariableDefinition);

            MemberExpressionNode watcherSetupUtilMemberExpression =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, _WATCHER_SETUP_UTIL, false);
            LiteralNullNode literalNull = nodeFactory.literalNull(-1);
            BinaryExpressionNode binaryExpression = nodeFactory.binaryExpression(Tokens.EQUALS_TOKEN,
                                                                                 watcherSetupUtilMemberExpression,
                                                                                 literalNull);
            ListNode test = nodeFactory.list(null, binaryExpression);

            Node watcherSetupUtilClassVariableDefinition = generateWatcherSetupUtilClassVariable();
            StatementListNode then = nodeFactory.statementList(null, watcherSetupUtilClassVariableDefinition);

            MemberExpressionNode base = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory,
                                                                                      WATCHER_SETUP_UTIL_CLASS, false);
            LiteralStringNode literalString = nodeFactory.literalString(INIT, false);
            ArgumentListNode initArgumentList = nodeFactory.argumentList(null, literalString);
            literalNull = nodeFactory.literalNull(-1);
            ArgumentListNode nullArgumentList = nodeFactory.argumentList(null, literalNull);
            CallExpressionNode initSelector =
                (CallExpressionNode) nodeFactory.callExpression(initArgumentList, nullArgumentList);
            initSelector.setMode(Tokens.LEFTBRACKET_TOKEN);
            initSelector.setRValue(false);
            MemberExpressionNode initMemberExpression = nodeFactory.memberExpression(base, initSelector);
            ListNode list = nodeFactory.list(null, initMemberExpression);
            ExpressionStatementNode expressionStatement = nodeFactory.expressionStatement(list);
            then = nodeFactory.statementList(then, expressionStatement);

            Node ifStatement = nodeFactory.ifStatement(test, then, null);
            result = nodeFactory.statementList(result, ifStatement);

            MemberExpressionNode watcherSetupUtilBase =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, _WATCHER_SETUP_UTIL, false);

            IdentifierNode setupIdentifier = nodeFactory.identifier(SETUP, false);
            ThisExpressionNode thisExpression = nodeFactory.thisExpression(-1);
            ArgumentListNode setupArgumentList = nodeFactory.argumentList(null, thisExpression);

            FunctionCommonNode propertyGetter = generatePropertyGetterFunction();
            setupArgumentList = nodeFactory.argumentList(setupArgumentList, propertyGetter);

            FunctionCommonNode staticPropertyGetter = generateStaticPropertyGetterFunction();
            setupArgumentList = nodeFactory.argumentList(setupArgumentList, staticPropertyGetter);

            MemberExpressionNode bindingsMemberExpression =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, BINDINGS, false);
            setupArgumentList = nodeFactory.argumentList(setupArgumentList, bindingsMemberExpression);

            MemberExpressionNode watchersMemberExpression =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, WATCHERS, false);
            setupArgumentList = nodeFactory.argumentList(setupArgumentList, watchersMemberExpression);

            CallExpressionNode setupSelector = (CallExpressionNode) nodeFactory.callExpression(setupIdentifier,
                                                                                               setupArgumentList);
            setupSelector.setRValue(false);
            MemberExpressionNode memberExpression = nodeFactory.memberExpression(watcherSetupUtilBase,
                                                                                 setupSelector);
            ListNode setupList = nodeFactory.list(null, memberExpression);
            ExpressionStatementNode setupExpressionStatement = nodeFactory.expressionStatement(setupList);
            result = nodeFactory.statementList(result, setupExpressionStatement);

            ExpressionStatementNode bindingsExpressionStatement = generateBindingsAssignment();
            result = nodeFactory.statementList(result, bindingsExpressionStatement);

            ExpressionStatementNode watchersExpressionStatement = generateWatchersAssignment();
            result = nodeFactory.statementList(result, watchersExpressionStatement);
        }

        return result;
    }

    private StatementListNode generateInitialBindingExecutions(StatementListNode statementList)
    {
    	StatementListNode result = statementList;

        if (mxmlDocument.getBindingExpressions().size() > 0)
        {
        	int kind = Tokens.VAR_TOKEN;
            QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, I);
            MemberExpressionNode uintMemberExpression =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, UINT, false);
            TypeExpressionNode typeExpression = nodeFactory.typeExpression(uintMemberExpression, true,
                                                                           false, -1);
            TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier,
                                                                              typeExpression);
            LiteralNumberNode initializer = nodeFactory.literalNumber(0);
            VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind,
                                                                              typedIdentifier, initializer);
            ListNode initializeList = nodeFactory.list(null, variableBinding);
            Node variableDefinition = nodeFactory.variableDefinition(null, kind, initializeList);
            result = nodeFactory.statementList(result, variableDefinition);

            Node forStatement = generateBindingsForLoop();
            result = nodeFactory.statementList(result, forStatement);
        }
        return result;
    }
    
    private StatementListNode generateStatesInitializers(StatementListNode statementList)
    {
        StatementListNode result = statementList;
        if (mxmlDocument.getVersion() >= 4)
        {
            StatesGenerator generator = new StatesGenerator(standardDefs);
            result = generator.getStatesASTInitializers(mxmlDocument.getStatefulModel(), nodeFactory,
                                                        configNamespaces, generateDocComments, statementList);
        }
        return result;
    }

    private StatementListNode generateConstructor(StatementListNode statementList, boolean generateDocComments,
                                                  String className, ParameterListNode parameterList, 
                                                  StatementListNode constructorStatementList)
    {
        StatementListNode result = statementList;

        if (generateDocComments && !processComments)
        {
            DocCommentNode docComment =
                AbstractSyntaxTreeUtil.generatePrivateDocComment(nodeFactory);
            result = nodeFactory.statementList(result, docComment);
        }
        
        if(processComments) // in some cases *not* having a private comment is not enough. So adding a blank comment to the c'tor
        {
            DocCommentNode docComment =
                AbstractSyntaxTreeUtil.generateDocComment(nodeFactory, "<description><![CDATA[]]></description>".intern());
            result = nodeFactory.statementList(result, docComment);
        }

        int position = AbstractSyntaxTreeUtil.lineNumberToPosition(context.getNodeFactory(), mxmlDocument.getRoot().getXmlLineNumber());
        
        FunctionDefinitionNode constructor =
            AbstractSyntaxTreeUtil.generateConstructor(context, className, parameterList, true, 
            		constructorStatementList, position);
        result = nodeFactory.statementList(result, constructor);

        return result;
    }

    private FunctionCommonNode generateDestinationFunction(BindingExpression bindingExpression)
    {
        String destinationTypeName = bindingExpression.getDestinationTypeName();
        ParameterNode parameter = AbstractSyntaxTreeUtil.generateParameter(nodeFactory,
                                                                           _SOURCE_FUNCTION_RETURN_VALUE,
                                                                           destinationTypeName, true);
        ParameterListNode parameterList = nodeFactory.parameterList(null, parameter);
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(parameterList, null);
        functionSignature.void_anno = true;
        StatementListNode body = null;
        String text;

        if (bindingExpression.isDestinationObjectProxy())
        {
            //${bindingExpression.getDestinationPath(true)} = new mx.utils.ObjectProxy(_sourceFunctionReturnValue);
            text = (bindingExpression.getDestinationPath(true) +
                    " = new " + standardDefs.getUtilsPackage() + ".ObjectProxy(_sourceFunctionReturnValue)");
        }
        else
        {
            //${bindingExpression.getNamespaceDeclarations()}
            if (bindingExpression.getNamespaceDeclarations().length() > 0)
            {
                body = bindingExpression.generateNamespaceDeclarations(context, body);
            }

            //${bindingExpression.getDestinationPath(true)} = _sourceFunctionReturnValue;
            text = (bindingExpression.getDestinationPath(true) +
                    " = _sourceFunctionReturnValue");
        }

        int xmlLineNumber = bindingExpression.getXmlLineNumber();
        List<Node> nodeList =
            AbstractSyntaxTreeUtil.parseExpression(context, configNamespaces, text,
                                                   xmlLineNumber, generateDocComments);
        
        if (!nodeList.isEmpty())
        {
            body = nodeFactory.statementList(body, nodeList.get(0));
        }

        return nodeFactory.functionCommon(context, null, functionSignature, body);
    }

    private ExpressionStatementNode generateDocumentAssignment()
    {
        QualifiedIdentifierNode qualifiedIdentifier =
            AbstractSyntaxTreeUtil.generateMxInternalQualifiedIdentifier(nodeFactory, _DOCUMENT, false);
        ThisExpressionNode thisExpression = nodeFactory.thisExpression(-1);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, thisExpression);
        SetExpressionNode setExpression = nodeFactory.setExpression(qualifiedIdentifier,
                                                                    argumentList, false);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(null, setExpression);
        ListNode list = nodeFactory.list(null, memberExpression);
        return nodeFactory.expressionStatement(list);
    }

    private StatementListNode generateEmbeds(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        Iterator<AtEmbed> iterator = mxmlDocument.getAtEmbeds().iterator();

        while (iterator.hasNext())
        {
            AtEmbed atEmbed = iterator.next();
            Map<String, Object> attributes = atEmbed.getAttributes();
            MetaDataNode metaData =
                AbstractSyntaxTreeUtil.generateMetaData(nodeFactory, EMBED, attributes);
            result = nodeFactory.statementList(result, metaData);

            MemberExpressionNode memberExpression =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory,
                                                              atEmbed.getType(),
                                                              true);
            TypeExpressionNode typeExpression =
                nodeFactory.typeExpression(memberExpression, true, false, -1);
            Node variableDefinition =
                AbstractSyntaxTreeUtil.generatePrivateVariable(nodeFactory,
                                                               typeExpression,
                                                               atEmbed.getPropName());
            result = nodeFactory.statementList(result, variableDefinition);
        }

        return result;
    }

    private StatementListNode generateEventInitializers(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        Iterator<Initializer> iterator = mxmlDocument.getRoot().getEventInitializerIterator();

        while (iterator.hasNext())
        {
            Initializer initializer = iterator.next();
            ThisExpressionNode thisExpression = nodeFactory.thisExpression(-1);
            result = initializer.generateAssignExpr(nodeFactory, configNamespaces, generateDocComments,
                                                    result, thisExpression);
        }

        return result;
    }

    private StatementListNode generateImports(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        Iterator<String[]> splitImportIterator = mxmlDocument.getSplitImports().iterator();

        while (splitImportIterator.hasNext())
        {
            String[] splitImport = splitImportIterator.next();
            ImportDirectiveNode importDirective = AbstractSyntaxTreeUtil.generateImport(context, splitImport);
            result = nodeFactory.statementList(result, importDirective);
        }

        Iterator<DocumentInfo.NameInfo> iterator = mxmlDocument.getImports().iterator();

        while (iterator.hasNext())
        {
            DocumentInfo.NameInfo nameInfo = iterator.next();
            int position = AbstractSyntaxTreeUtil.lineNumberToPosition(nodeFactory, nameInfo.getLine());
            ImportDirectiveNode importDirective = AbstractSyntaxTreeUtil.generateImport(context, nameInfo.getName(), position);            
            result = nodeFactory.statementList(result, importDirective);
        }

        return result;
    }

    private StatementListNode generateInitializerSupportDefs(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        Iterator<Initializer> topLevelInitializerIterator = mxmlDocument.getTopLevelInitializerIterator();

        // Top level document initializer iterator.
        while (topLevelInitializerIterator.hasNext())
        {
            result = topLevelInitializerIterator.next().generateDefinitions(context, configNamespaces,
                                                                            generateDocComments, result);
        }

        Iterator<Initializer> eventInitializerIterator = mxmlDocument.getStatefulEventIterator();

        // Stateful event initializer iterator.
        while (eventInitializerIterator.hasNext())
        {
            result = eventInitializerIterator.next().generateDefinitions(context, configNamespaces,
                                                                         generateDocComments, result);
        }
        
        Iterator<Initializer> rootSubInitializerIterator = mxmlDocument.getRoot().getSubInitializerIterator();

        // Root sub-initializer iterator.
        while (rootSubInitializerIterator.hasNext())
        {
            Initializer initializer = rootSubInitializerIterator.next();
            result = initializer.generateDefinitions(context, configNamespaces, generateDocComments, result);
        }

        // Stateful document initializers
        if (mxmlDocument.getVersion() >= 4)
        {
            Iterator<Initializer> statesSubInitializerIterator = mxmlDocument.getStatefulModel().getSubInitializerIterators();
            while (statesSubInitializerIterator.hasNext())
            {
                Initializer initializer = statesSubInitializerIterator.next();
                result = initializer.generateDefinitions(context, configNamespaces, generateDocComments, result);
            }
        }
        
        if (mxmlDocument.getBindingExpressions().size() > 0)
        {
            result = generateBindingsSetup(result);
        }

        return result;
    }

    private FunctionDefinitionNode generateInitializeFunction()
    {
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(null, null);
        functionSignature.void_anno = true;

        StatementListNode statementList = null;

        if (mxmlDocument.getDescribeVisualChildren() && mxmlDocument.getIsContainer())
        {
            QualifiedIdentifierNode qualifiedIdentifier =
                AbstractSyntaxTreeUtil.generateMxInternalQualifiedIdentifier(nodeFactory,
                                                                             SET_DOCUMENT_DESCRIPTOR,
                                                                             false);
            MemberExpressionNode getterSelector =
                AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory,
                                                              _DOCUMENT_DESCRIPTOR_,
                                                              false);
            ArgumentListNode argumentList = nodeFactory.argumentList(null, getterSelector);
            CallExpressionNode callExpression = (CallExpressionNode) nodeFactory.callExpression(qualifiedIdentifier,
                                                                                                argumentList);
            callExpression.setRValue(false);
            MemberExpressionNode memberExpression = nodeFactory.memberExpression(null, callExpression);
            ListNode list = nodeFactory.list(null, memberExpression);
            ExpressionStatementNode expressionStatement = nodeFactory.expressionStatement(list);
            statementList = nodeFactory.statementList(statementList, expressionStatement);
        }

        SuperExpressionNode superExpression = nodeFactory.superExpression(null, -1);
        IdentifierNode identifier = nodeFactory.identifier(INITIALIZE, false);
        CallExpressionNode callExpression = (CallExpressionNode) nodeFactory.callExpression(identifier, null);
        callExpression.setRValue(false);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(superExpression, callExpression);
        ListNode list = nodeFactory.list(null, memberExpression);
        ExpressionStatementNode expressionStatement = nodeFactory.expressionStatement(list);
        statementList = nodeFactory.statementList(statementList, expressionStatement);

        FunctionCommonNode functionCommon = nodeFactory.functionCommon(context, null, functionSignature, statementList);
        functionCommon.setUserDefinedBody(true);

        AttributeListNode attributeList = AbstractSyntaxTreeUtil.generateOverridePublicAttribute(nodeFactory);
        identifier = nodeFactory.identifier(INITIALIZE, false);
        FunctionNameNode functionName = nodeFactory.functionName(Tokens.EMPTY_TOKEN, identifier);

        return nodeFactory.functionDefinition(context, attributeList, functionName, functionCommon);
    }

    private StatementListNode generateInstanceVariables(StatementListNode statementList)
    {
        StatementListNode result = statementList;
        Iterator<PropertyDeclaration> iterator = mxmlDocument.getDeclarationIterator();

        while (iterator.hasNext())
        {
            PropertyDeclaration propertyDeclaration = iterator.next();

            if (propertyDeclaration.getInspectable())
            {
                MetaDataNode inspectableMetaData = AbstractSyntaxTreeUtil.generateMetaData(nodeFactory, INSPECTABLE);
                result = nodeFactory.statementList(result, inspectableMetaData);
            }

            if (!propertyDeclaration.getIdIsAutogenerated() || propertyDeclaration.getBindabilityEnsured())
            {
            	MetaDataNode bindableMetaData = AbstractSyntaxTreeUtil.generateMetaData(nodeFactory, BINDABLE);
                result = nodeFactory.statementList(result, bindableMetaData);
            }
            
            if (!propertyDeclaration.getIdIsAutogenerated())
            {
                if(processComments)
                {
                    MetaDataNode propertyDocComment = null;
                    if(propertyDeclaration.getComment() != null ) 
                    {
                        propertyDocComment = AbstractSyntaxTreeUtil.generateDocComment(nodeFactory, propertyDeclaration.getComment().intern());
                    }
                    
                    if (propertyDocComment != null)
                    {
                        result = nodeFactory.statementList(result, propertyDocComment);
                    }
                    else 
                    {
                        // when individual classes are listed using doc-classes, properties with id but no comment are not visible. So adding a blank comment.
                        DocCommentNode docComment =
                            AbstractSyntaxTreeUtil.generateDocComment(nodeFactory, "<description><![CDATA[]]></description>".intern());
                        result = nodeFactory.statementList(result, docComment);
                    }
                }
                
                if (generateDocComments && !processComments)
                {
                    DocCommentNode docComment = AbstractSyntaxTreeUtil.generatePrivateDocComment(nodeFactory);
                    result = nodeFactory.statementList(result, docComment);
                }                
            } 
            else 
            {
                if (generateDocComments)
                {
                    DocCommentNode docComment = AbstractSyntaxTreeUtil.generatePrivateDocComment(nodeFactory);
                    result = nodeFactory.statementList(result, docComment);
                }                
            }

            TypeExpressionNode typeExpression =
                AbstractSyntaxTreeUtil.generateTypeExpression(nodeFactory,
                                                              propertyDeclaration.getTypeExpr(), true);
            
            Node variableDefinition =
                AbstractSyntaxTreeUtil.generatePublicVariable(context, typeExpression,
                                                              propertyDeclaration.getName());
            result = nodeFactory.statementList(result, variableDefinition);
        }

        return result;
    }

    private ExpressionStatementNode generateIsTwoWayPrimaryAssignment(int leftValueId)
    {
        MemberExpressionNode leftResultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        LiteralNumberNode leftLiteralNumber = nodeFactory.literalNumber(leftValueId);;
        ArgumentListNode leftGetExpressionArgumentList = nodeFactory.argumentList(null, leftLiteralNumber);
        GetExpressionNode leftGetExpression = nodeFactory.getExpression(leftGetExpressionArgumentList);
        leftGetExpression.setMode(Tokens.LEFTBRACKET_TOKEN);
        MemberExpressionNode base = nodeFactory.memberExpression(leftResultMemberExpression, leftGetExpression);

        IdentifierNode twoWayCounterpartIdentifier = nodeFactory.identifier(IS_TWO_WAY_PRIMARY, false);
        LiteralBooleanNode literalBoolean = nodeFactory.literalBoolean(true);
        ArgumentListNode setExpressionArgumentList = nodeFactory.argumentList(null, literalBoolean);
        SetExpressionNode selector = nodeFactory.setExpression(twoWayCounterpartIdentifier,
                                                               setExpressionArgumentList, false);

        MemberExpressionNode memberExpression = nodeFactory.memberExpression(base, selector);
        ListNode list = nodeFactory.list(null, memberExpression);
        return nodeFactory.expressionStatement(list);
    }

    private MemberExpressionNode generateMxInternalGetterSelector(String name, boolean intern)
    {
        MemberExpressionNode mxInternalGetterSelector =
            AbstractSyntaxTreeUtil.generateResolvedGetterSelector(nodeFactory, standardDefs.getCorePackage(), MX_INTERNAL);
        return AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, mxInternalGetterSelector, name, true);
    }

    private DocCommentNode generatePackageDocComment(String packageName, String className, String path)
    {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<description><![CDATA[\n  Generated by mxmlc 4.0\n  Package: ");
        stringBuilder.append(packageName);
        stringBuilder.append("\n Class:   ");
        stringBuilder.append(className);
        stringBuilder.append("\n Source:  ");
        stringBuilder.append(path);
        stringBuilder.append("\n ]]></description>");

        return AbstractSyntaxTreeUtil.generateDocComment(nodeFactory, stringBuilder.toString().intern());
    }

    private FunctionCommonNode generatePropertyGetterFunction()
    {
        ParameterNode parameter =
            AbstractSyntaxTreeUtil.generateParameter(nodeFactory, PROPERTY_NAME, STRING, false);
        ParameterListNode parameterList = nodeFactory.parameterList(null, parameter);
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(parameterList, null);

        MemberExpressionNode base =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, TARGET, false);
        MemberExpressionNode argument =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, PROPERTY_NAME, false);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, argument);
        GetExpressionNode selector = nodeFactory.getExpression(argumentList);
        selector.setMode(Tokens.LEFTBRACKET_TOKEN);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(base, selector);
        ListNode list = nodeFactory.list(null, memberExpression);
        ReturnStatementNode returnStatement = nodeFactory.returnStatement(list);
        StatementListNode statementList = nodeFactory.statementList(null, returnStatement);

        return nodeFactory.functionCommon(context, null, functionSignature, statementList);
    }

    private StatementListNode generatePropertyInitializers(StatementListNode statementList, boolean stageProperties)
    {
        StatementListNode result = statementList;

        Iterator<Initializer> iterator = stageProperties ? mxmlDocument.getStagePropertyInitializerIterator()
                                                         : mxmlDocument.getNonStagePropertyInitializerIterator();

        while (iterator.hasNext())
        {
            Initializer initializer = iterator.next();
            ThisExpressionNode thisExpression = nodeFactory.thisExpression(-1);
            result = initializer.generateAssignExpr(nodeFactory, configNamespaces, generateDocComments, result, thisExpression);
        }

        return result;
    }
    
    private StatementListNode generateDesignLayerInitializers(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        Iterator<Initializer> iterator = mxmlDocument.getDesignLayerPropertyInitializerIterator();
                                                        
        while (iterator.hasNext())
        {
            Initializer initializer = iterator.next();
            ThisExpressionNode thisExpression = nodeFactory.thisExpression(-1);
            result = initializer.generateAssignExpr(nodeFactory, configNamespaces, generateDocComments, result, thisExpression);
        }

        return result;
    }

    private ExpressionStatementNode generatePush(String variable, Node node)
    {
        MemberExpressionNode variableMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, variable, false);
        IdentifierNode identifier = nodeFactory.identifier(PUSH);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, node);
        CallExpressionNode callExpression =
            (CallExpressionNode) nodeFactory.callExpression(identifier, argumentList);
        callExpression.setRValue(false);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(variableMemberExpression,
                callExpression);
        ListNode list = nodeFactory.list(null, memberExpression);
        return nodeFactory.expressionStatement(list);
    }

    private Node generateResultVariable()
    {
        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, RESULT);
        MemberExpressionNode memberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, ARRAY, false);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, typeExpression);
        LiteralArrayNode literalArray = nodeFactory.literalArray(null);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier,
                                                                          literalArray);
        ListNode list = nodeFactory.list(null, variableBinding);
        return nodeFactory.variableDefinition(null, kind, list);
    }

    private StatementListNode generateSetWatcherSetupUtilFunction(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        if (generateDocComments)
        {
            DocCommentNode docComment = AbstractSyntaxTreeUtil.generatePrivateDocComment(nodeFactory);
            result = nodeFactory.statementList(result, docComment);
        }

        AttributeListNode attributeList = AbstractSyntaxTreeUtil.generatePublicStaticAttribute(nodeFactory);
        IdentifierNode watcherSetupUtilIdentifier = nodeFactory.identifier(WATCHER_SETUP_UTIL, false);
        FunctionNameNode functionName = nodeFactory.functionName(Tokens.SET_TOKEN, watcherSetupUtilIdentifier);

        ParameterNode parameter =
            AbstractSyntaxTreeUtil.generateParameter(nodeFactory, WATCHER_SETUP_UTIL,
                                                     IWATCHER_SETUP_UTIL, false);
        ParameterListNode parameterList = nodeFactory.parameterList(null, parameter);
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(parameterList, null);
        functionSignature.void_anno = true;

        MemberExpressionNode classMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, mxmlDocument.getClassName(), true);
        ListNode base = nodeFactory.list(null, classMemberExpression);
        IdentifierNode identifier = nodeFactory.identifier(_WATCHER_SETUP_UTIL, false);
        MemberExpressionNode watcherSetupUtilMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, WATCHER_SETUP_UTIL, false);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, watcherSetupUtilMemberExpression);
        SetExpressionNode selector = nodeFactory.setExpression(identifier, argumentList, false);
        selector.setRValue(false);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(base, selector);
        ListNode list = nodeFactory.list(null, memberExpression);
        ExpressionStatementNode expressionStatement = nodeFactory.expressionStatement(list);
        StatementListNode functionStatementList = nodeFactory.statementList(null, expressionStatement);
        FunctionCommonNode functionCommon = nodeFactory.functionCommon(context, null,
                                                                       functionSignature,
                                                                       functionStatementList);
        functionCommon.setUserDefinedBody(true);

        FunctionDefinitionNode functionDefinition = nodeFactory.functionDefinition(context, attributeList,
                                                                                   functionName, functionCommon);
        return nodeFactory.statementList(result, functionDefinition);
    }

    private FunctionCommonNode generateSourceFunction(BindingExpression bindingExpression)
    {
        String destinationTypeName = bindingExpression.getDestinationTypeName();
        TypeExpressionNode returnType = null;

        if (!destinationTypeName.equals(ASTERISK))
        {
            MemberExpressionNode memberExpression =
                AbstractSyntaxTreeUtil.generateMemberExpression(nodeFactory, destinationTypeName);
            returnType = nodeFactory.typeExpression(memberExpression, true, false, -1);
        }

        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(null, returnType);
        StatementListNode body = null;
        String text = bindingExpression.getSourceExpression();
        int xmlLineNumber = bindingExpression.getXmlLineNumber();
        List<Node> nodeList =
            AbstractSyntaxTreeUtil.parseExpression(context, configNamespaces, text,
                                                   xmlLineNumber, generateDocComments);
        
        if (!nodeList.isEmpty())
        {
            ExpressionStatementNode sourceExpressionStatement = (ExpressionStatementNode) nodeList.get(0);
            ListNode list = (ListNode) sourceExpressionStatement.expr;

            if (destinationTypeName.equals(STRING))
            {
                body = generateSourceFunctionStringConversion(body, destinationTypeName, list.items.get(0));
            }
            else if (destinationTypeName.equals(ARRAY))
            {
                body = generateSourceFunctionArrayConversion(body, destinationTypeName, list.items.get(0));
            }
            else
            {
                //if (${bindingExpression.getTwoWayCounterpart()})
                //    ${bindingExpression.getTwoWayCounterpart().getNamespaceDeclarations()}
                if (bindingExpression.getTwoWayCounterpart() != null &&
                    bindingExpression.getTwoWayCounterpart().getNamespaceDeclarations().length() > 0)
                {
                    body = bindingExpression.getTwoWayCounterpart().generateNamespaceDeclarations(context, body);
                }

                //return $bindingExpression.sourceExpression;
                ReturnStatementNode returnStatement = nodeFactory.returnStatement(list);
                body = nodeFactory.statementList(body, returnStatement);
            }
        }

        return nodeFactory.functionCommon(context, null, functionSignature, body);
    }

    private StatementListNode generateSourceFunctionArrayConversion(StatementListNode statementList,
                                                                    String destinationTypeName,
                                                                    Node initializer)
    {
        // return ((result == null) || (result is Array) || (result is flash.utils.Proxy) ? result : [result]);
        StatementListNode result = statementList;

        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, RESULT);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, null);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier,
                                                                          initializer);
        ListNode variableList = nodeFactory.list(null, variableBinding);
        Node variableDefinition = nodeFactory.variableDefinition(null, kind, variableList);
        result = nodeFactory.statementList(result, variableDefinition);

        MemberExpressionNode resultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        LiteralNullNode literalNull = nodeFactory.literalNull(-1);
        BinaryExpressionNode binaryExpression = nodeFactory.binaryExpression(Tokens.EQUALS_TOKEN,
                                                                             resultMemberExpression,
                                                                             literalNull);

        resultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        MemberExpressionNode arrayMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, ARRAY, false);
        BinaryExpressionNode arrayBinaryExpression =
            nodeFactory.binaryExpression(Tokens.IS_TOKEN, resultMemberExpression, arrayMemberExpression);
        ListNode innerBinaryExpressionList = nodeFactory.list(null, arrayBinaryExpression);

        BinaryExpressionNode innerBinaryExpression =
            nodeFactory.binaryExpression(Tokens.LOGICALOR_TOKEN, binaryExpression, innerBinaryExpressionList);

        resultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        MemberExpressionNode proxyMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, StandardDefs.PACKAGE_FLASH_UTILS, PROXY, false);
        BinaryExpressionNode proxyBinaryExpression =
            nodeFactory.binaryExpression(Tokens.IS_TOKEN, resultMemberExpression, proxyMemberExpression);
        ListNode outerBinaryExpressionList = nodeFactory.list(null, proxyBinaryExpression);

        BinaryExpressionNode outerBinaryExpression =
            nodeFactory.binaryExpression(Tokens.LOGICALOR_TOKEN, innerBinaryExpression, outerBinaryExpressionList);

        resultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, resultMemberExpression);
        LiteralArrayNode literalArray = nodeFactory.literalArray(argumentList);

        ConditionalExpressionNode conditionalExpression =
            nodeFactory.conditionalExpression(outerBinaryExpression, resultMemberExpression, literalArray);
        ListNode returnList = nodeFactory.list(null, conditionalExpression);
        ListNode returnListList = nodeFactory.list(null, returnList);
        ReturnStatementNode returnStatement = nodeFactory.returnStatement(returnListList);
        result = nodeFactory.statementList(result, returnStatement);

        return result;
    }

    private StatementListNode generateSourceFunctionStringConversion(StatementListNode statementList,
                                                                     String destinationTypeName,
                                                                     Node initializer)
    {
        // return (result == undefined ? null : String(result));
        StatementListNode result = statementList;

        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, RESULT);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, null);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier,
                                                                          initializer);
        ListNode variableList = nodeFactory.list(null, variableBinding);
        Node variableDefinition = nodeFactory.variableDefinition(null, kind, variableList);
        result = nodeFactory.statementList(result, variableDefinition);

        MemberExpressionNode resultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        MemberExpressionNode undefinedMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, UNDEFINED, false);
        BinaryExpressionNode binaryExpression = nodeFactory.binaryExpression(Tokens.EQUALS_TOKEN,
                                                                             resultMemberExpression,
                                                                             undefinedMemberExpression);
        LiteralNullNode literalNull = nodeFactory.literalNull(-1);

        IdentifierNode stringIdentifier = nodeFactory.identifier(destinationTypeName, false);
        resultMemberExpression = AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, resultMemberExpression);
        CallExpressionNode callExpression = (CallExpressionNode) nodeFactory.callExpression(stringIdentifier,
                                                                                            argumentList);
        callExpression.setRValue(false);
        MemberExpressionNode stringMemberExpression = nodeFactory.memberExpression(null, callExpression);
        ConditionalExpressionNode conditionalExpression =
            nodeFactory.conditionalExpression(binaryExpression, literalNull, stringMemberExpression);
        ListNode returnList = nodeFactory.list(null, conditionalExpression);
        ListNode returnListList = nodeFactory.list(null, returnList);
        ReturnStatementNode returnStatement = nodeFactory.returnStatement(returnListList);
        result = nodeFactory.statementList(result, returnStatement);

        return result;
    }

    private AttributeListNode generateMxInternalAttributeList()
    {
        MemberExpressionNode mxInternalGetterSelector =
            AbstractSyntaxTreeUtil.generateResolvedGetterSelector(nodeFactory, standardDefs.getCorePackage(), MX_INTERNAL);
        ListNode list = nodeFactory.list(null, mxInternalGetterSelector);
        return nodeFactory.attributeList(list, null);
    }

    private QualifiedIdentifierNode generateMxInternalQualifiedIdentifier(String name, boolean intern)
    {
        if (intern)
        {
            name = name.intern();
    }
        return nodeFactory.qualifiedIdentifier(generateMxInternalAttributeList(), name);
    }

    private FunctionCommonNode generateStaticPropertyGetterFunction()
    {
        ParameterNode parameter =
            AbstractSyntaxTreeUtil.generateParameter(nodeFactory, PROPERTY_NAME, STRING, false);
        ParameterListNode parameterList = nodeFactory.parameterList(null, parameter);
        FunctionSignatureNode functionSignature = nodeFactory.functionSignature(parameterList, null);

        MemberExpressionNode base =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, mxmlDocument.getClassName(), true);
        MemberExpressionNode argument =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, PROPERTY_NAME, false);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, argument);
        GetExpressionNode selector = nodeFactory.getExpression(argumentList);
        selector.setMode(Tokens.LEFTBRACKET_TOKEN);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(base, selector);
        ListNode list = nodeFactory.list(null, memberExpression);
        ReturnStatementNode returnStatement = nodeFactory.returnStatement(list);
        StatementListNode statementList = nodeFactory.statementList(null, returnStatement);

        return nodeFactory.functionCommon(context, null, functionSignature, statementList);
    }

    private Node generateTargetVariable()
    {
        //var target:Object = this;
        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, TARGET);
        MemberExpressionNode memberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, OBJECT, true);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, typeExpression);
        ThisExpressionNode thisExpression = nodeFactory.thisExpression(-1);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier,
                                                                          thisExpression);
        ListNode list = nodeFactory.list(null, variableBinding);
        return nodeFactory.variableDefinition(null, kind, list);
    }

    private ExpressionStatementNode generateTwoWayCounterpartAssignment(int leftValueId, int rightValueId)
    {
        MemberExpressionNode leftResultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        LiteralNumberNode leftLiteralNumber = nodeFactory.literalNumber(leftValueId);;
        ArgumentListNode leftGetExpressionArgumentList = nodeFactory.argumentList(null, leftLiteralNumber);
        GetExpressionNode leftGetExpression = nodeFactory.getExpression(leftGetExpressionArgumentList);
        leftGetExpression.setMode(Tokens.LEFTBRACKET_TOKEN);
        MemberExpressionNode base = nodeFactory.memberExpression(leftResultMemberExpression, leftGetExpression);

        IdentifierNode twoWayCounterpartIdentifier = nodeFactory.identifier(TWO_WAY_COUNTERPART, false);
        MemberExpressionNode rightResultMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, RESULT, false);
        LiteralNumberNode rightLiteralNumber = nodeFactory.literalNumber(rightValueId);;
        ArgumentListNode rightGetExpressionArgumentList = nodeFactory.argumentList(null, rightLiteralNumber);
        GetExpressionNode rightGetExpression = nodeFactory.getExpression(rightGetExpressionArgumentList);
        rightGetExpression.setMode(Tokens.LEFTBRACKET_TOKEN);
        MemberExpressionNode rightValueMemberExpression =
            nodeFactory.memberExpression(rightResultMemberExpression, rightGetExpression);
        ArgumentListNode setExpressionArgumentList = nodeFactory.argumentList(null, rightValueMemberExpression);
        SetExpressionNode selector = nodeFactory.setExpression(twoWayCounterpartIdentifier,
                                                               setExpressionArgumentList, false);

        MemberExpressionNode memberExpression = nodeFactory.memberExpression(base, selector);
        ListNode list = nodeFactory.list(null, memberExpression);
        return nodeFactory.expressionStatement(list);
    }

    private StatementListNode generateTypeImportDummies(StatementListNode statementList)
    {
        StatementListNode result = statementList;

        Iterator<String> iterator = mxmlDocument.getTypeRefs().iterator();
        int index = 1;

        while (iterator.hasNext())
        {
            TypeExpressionNode typeExpression =
                AbstractSyntaxTreeUtil.generateTypeExpression(nodeFactory, iterator.next(), true);
            Node variableDefinition =
                AbstractSyntaxTreeUtil.generatePrivateVariable(nodeFactory, typeExpression,
                                                               "_typeRef" + index);
            result = nodeFactory.statementList(result, variableDefinition);
            index++;
        }

        return result;
    }

    private ExpressionStatementNode generateWatchersAssignment()
    {
        MemberExpressionNode mxInternalGetterSelector =
            AbstractSyntaxTreeUtil.generateResolvedGetterSelector(nodeFactory, standardDefs.getCorePackage(), MX_INTERNAL);
        QualifiedIdentifierNode qualifiedIdentifier =
            AbstractSyntaxTreeUtil.generateQualifiedIdentifier(nodeFactory, mxInternalGetterSelector,
                                                               _WATCHERS, false);
        MemberExpressionNode rvalueBase = generateMxInternalGetterSelector(_WATCHERS, false);
        IdentifierNode concatIdentifier = nodeFactory.identifier(CONCAT, false);
        MemberExpressionNode watchersMemberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, WATCHERS, false);
        ArgumentListNode concatArgumentList = nodeFactory.argumentList(null, watchersMemberExpression);
        CallExpressionNode callExpression =
            (CallExpressionNode) nodeFactory.callExpression(concatIdentifier, concatArgumentList);
        callExpression.setRValue(false);
        MemberExpressionNode argument = nodeFactory.memberExpression(rvalueBase, callExpression);
        ArgumentListNode argumentList = nodeFactory.argumentList(null, argument);
        SetExpressionNode setExpression = nodeFactory.setExpression(qualifiedIdentifier, argumentList, false);
        MemberExpressionNode memberExpression = nodeFactory.memberExpression(null, setExpression);
        ListNode list = nodeFactory.list(null, memberExpression);
        return nodeFactory.expressionStatement(list);
    }

    private Node generateWatchersVariable()
    {
        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, WATCHERS);
        MemberExpressionNode memberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, ARRAY, false);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, typeExpression);
        LiteralArrayNode literalArray = nodeFactory.literalArray(null);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier,
                                                                          literalArray);
        ListNode list = nodeFactory.list(null, variableBinding);
        return nodeFactory.variableDefinition(null, kind, list);
    }

    private Node generateWatcherSetupUtilClassVariable()
    {
        int kind = Tokens.VAR_TOKEN;
        QualifiedIdentifierNode qualifiedIdentifier = nodeFactory.qualifiedIdentifier(null, WATCHER_SETUP_UTIL_CLASS);
        MemberExpressionNode memberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, OBJECT, false);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        TypedIdentifierNode typedIdentifier = nodeFactory.typedIdentifier(qualifiedIdentifier, typeExpression);

        IdentifierNode getDefinitionByNameIdentifier = nodeFactory.identifier(GET_DEFINITION_BY_NAME, false);
        LiteralStringNode literalString = nodeFactory.literalString(mxmlDocument.getWatcherSetupUtilClassName());
        ArgumentListNode argumentList = nodeFactory.argumentList(null, literalString);
        CallExpressionNode callExpression =
            (CallExpressionNode) nodeFactory.callExpression(getDefinitionByNameIdentifier, argumentList);
        callExpression.setRValue(false);
        MemberExpressionNode initializer = nodeFactory.memberExpression(null, callExpression);
        VariableBindingNode variableBinding = nodeFactory.variableBinding(null, kind, typedIdentifier,
                                                                          initializer);
        ListNode list = nodeFactory.list(null, variableBinding);
        return nodeFactory.variableDefinition(null, kind, list);
    }

	/**
	 * wrapper for generating entire descriptor tree. See notes on includePropNames param below.
	 */
	private static MemberExpressionNode getDescriptorInitializerFragments(NodeFactory nodeFactory, HashSet<String>configNamespaces,
                                                                          boolean generateDocComments, Model model)
	{
        return addDescriptorInitializerFragments(nodeFactory, configNamespaces, generateDocComments, model,
                                                 FrameworkDefs.requiredTopLevelDescriptorProperties,
                                                 true);
	}

    String getPath()
    {
        return mxmlDocument.getSourcePath();
    }
    
    /**
     *  private var __moduleFactoryInitialized:Boolean = false;
	 *
     * @return
     */
    private VariableDefinitionNode generateModuleFactoryInitializedVariable()
    {
    	
        MemberExpressionNode memberExpression =
            AbstractSyntaxTreeUtil.generateGetterSelector(nodeFactory, BOOLEAN, false);
        TypeExpressionNode typeExpression = nodeFactory.typeExpression(memberExpression, true, false, -1);
        LiteralBooleanNode literalBoolean = nodeFactory.literalBoolean(false);
    	Node variableDefinition = AbstractSyntaxTreeUtil.generatePrivateVariable(nodeFactory, 
    														typeExpression,
    														__MODULE_FACTORY_INITIALIZED,
    														literalBoolean);
        return (VariableDefinitionNode)variableDefinition;
    }
    
}
