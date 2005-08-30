/*
  (c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP, all rights reserved.
  [See end of file]
  $Id: TestNodeToTriplesMap.java,v 1.15 2005-08-30 13:29:48 chris-dollin Exp $
*/
package com.hp.hpl.jena.graph.test;

import java.util.*;

import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.Triple.*;
import com.hp.hpl.jena.mem.NodeToTriplesMap;

import junit.framework.TestSuite;

/**
 	TestNodeToTriplesMap: added, post-hoc, by kers once NTM got
 	rather complicated. So these tests may be (are, at the moment)
 	incomplete.
 	
 	@author kers
*/
public class TestNodeToTriplesMap extends GraphTestBase
    {
    public TestNodeToTriplesMap( String name )
        { super( name ); }
    
    public static TestSuite suite()
        { return new TestSuite( TestNodeToTriplesMap.class ); }
    
    protected NodeToTriplesMap ntS = new NodeToTriplesMap( Field.getSubject, Field.getPredicate, Field.getObject );
    	
    protected NodeToTriplesMap ntP = new NodeToTriplesMap( Field.getPredicate, Field.getObject, Field.getSubject );
    	
    protected NodeToTriplesMap ntO = new NodeToTriplesMap( Field.getObject, Field.getPredicate, Field.getSubject );

    protected static final Node x = node( "x" );
    
    protected static final Node y = node( "y" );
    
    public void testZeroSize()
        {
        testZeroSize( "fresh NTM", ntS );
        }
    
    protected void testZeroSize( String title, NodeToTriplesMap nt )
        {
        assertEquals( title + " should have size 0", 0, nt.size() );
        assertEquals( title + " should be isEmpty()", true, nt.isEmpty() );
        assertEquals( title + " should have empty domain", false, nt.domain().hasNext() );
        }
    
    public void testAddOne()
        {
        ntS.add( triple( "x P y" ) );
        testJustOne( x, ntS );
        }
    
    public void testAddOneTwice()
        {
        addTriples( ntS, "x P y; x P y" );
        testJustOne( x, ntS );
        }
    
    protected void testJustOne( Node x, NodeToTriplesMap nt )
        {
        assertEquals( 1, nt.size() );
        assertEquals( false, nt.isEmpty() );
        assertEquals( just( x ), iteratorToSet( nt.domain() ) );
        }
    
    public void testAddTwoUnshared()
        {
        addTriples( ntS, "x P a; y Q b" );
        assertEquals( 2, ntS.size() );
        assertEquals( false, ntS.isEmpty() );
        assertEquals( both( x, y ), iteratorToSet( ntS.domain() ) );
        }
    
    public void testAddTwoShared()
        {
        addTriples( ntS, "x P a; x Q b" );
        assertEquals( 2, ntS.size() );
        assertEquals( false, ntS.isEmpty() );
        assertEquals( just( x ), iteratorToSet( ntS.domain() ) );
        }
    
    public void testClear()
        {
        addTriples( ntS, "x P a; x Q b; y R z" );
        ntS.clear();
        testZeroSize( "cleared NTM", ntS );
        }
    
    public void testAllIterator()
        {
        String triples = "x P b; y P d; y P f";
        addTriples( ntS, triples );
        assertEquals( tripleSet( triples ), iteratorToSet( ntS.iterateAll() ) );
        }
    
    public void testOneIterator()
        {
        addTriples( ntS, "x P b; y P d; y P f" );
        assertEquals( tripleSet( "x P b" ), iteratorToSet( ntS.iterator( x ) ) );
        assertEquals( tripleSet( "y P d; y P f" ), iteratorToSet( ntS.iterator( y ) ) );
        }
    
    public void testRemove()
        {
        addTriples( ntS, "x P b; y P d; y R f" );
        ntS.remove( triple( "y P d" ) );
        assertEquals( 2, ntS.size() );
        assertEquals( tripleSet( "x P b; y R f" ), iteratorToSet( ntS.iterateAll() ) );
        }
    
    public void testRemoveByIterator()
        {
        addTriples( ntS, "x nice a; a nasty b; x nice c" );
        addTriples( ntS, "y nice d; y nasty e; y nice f" );
        Iterator it = ntS.iterateAll();
        while (it.hasNext())
            {
            Triple t = (Triple) it.next();
            if (t.getPredicate().equals( node( "nasty") )) it.remove();
            }
        assertEquals( tripleSet( "x nice a; x nice c; y nice d; y nice f" ), iteratorToSet( ntS.iterateAll() ) );
        }
    
    public void testIteratorWIthPatternOnEmpty()
        {
        assertEquals( tripleSet( "" ), iteratorToSet( ntS.iterateAll( triple( "a P b" ) ) ) );
        }

    public void testIteratorWIthPatternOnSomething()
        {
        addTriples( ntS, "x P a; y P b; y R c" );
        assertEquals( tripleSet( "x P a" ), iteratorToSet( ntS.iterateAll( triple( "x P ??" ) ) ) );
        assertEquals( tripleSet( "y P b; y R c" ), iteratorToSet( ntS.iterateAll( triple( "y ?? ??" ) ) ) );
        assertEquals( tripleSet( "x P a; y P b" ), iteratorToSet( ntS.iterateAll( triple( "?? P ??" ) ) ) );
        assertEquals( tripleSet( "y R c" ), iteratorToSet( ntS.iterateAll( triple( "?? ?? c" ) ) ) );
        }

    public void testUnspecificRemoveS()
        {
        addTriples( ntS, "x P a; y Q b; z R c" );
        ntS.remove( triple( "x P a" ) );
        assertEquals( tripleSet( "y Q b; z R c" ), iteratorToSet( ntS.iterateAll() ) );
        }
    
    public void testUnspecificRemoveP()
        {
        addTriples( ntP, "x P a; y Q b; z R c" );
        ntP.remove( triple( "y Q b" ) );
        assertEquals( tripleSet( "x P a; z R c" ), iteratorToSet( ntP.iterateAll() ) );
        }
    
    public void testUnspecificRemoveO()
        {
        addTriples( ntO, "x P a; y Q b; z R c" );
        ntO.remove( triple( "z R c" ) );
        assertEquals( tripleSet( "x P a; y Q b" ), iteratorToSet( ntO.iterateAll() ) );
        }
    
    public void testAddBooleanResult()
        {
        assertEquals( true, ntS.add( triple( "x P y" ) ) );
        assertEquals( false, ntS.add( triple( "x P y" ) ) );
    /* */
        assertEquals( true, ntS.add( triple( "y Q z" ) ) );
        assertEquals( false, ntS.add( triple( "y Q z" ) ) );
    /* */
        assertEquals( true, ntS.add( triple( "y R s" ) ) );
        assertEquals( false, ntS.add( triple( "y R s" ) ) );
        }
    
    public void testRemoveBooleanResult()
        {
        assertEquals( false, ntS.remove( triple( "x P y" ) ) );
        ntS.add( triple( "x P y" ) );
        assertEquals( false, ntS.remove( triple( "x Q y" ) ) );
        assertEquals( true, ntS.remove( triple( "x P y" ) ) );
        assertEquals( false, ntS.remove( triple( "x P y" ) ) );
        }
    
    public void testContains()
        {
        addTriples( ntS, "x P y; a P b" );
        assertTrue( ntS.contains( triple( "x P y" ) ) );
        assertTrue( ntS.contains( triple( "a P b" ) ) );
        assertFalse( ntS.contains( triple( "x P z" ) ) );
        assertFalse( ntS.contains( triple( "y P y" ) ) );
        assertFalse( ntS.contains( triple( "x R y" ) ) );
        assertFalse( ntS.contains( triple( "e T f" ) ) );
        assertFalse( ntS.contains( triple( "_x F 17" ) ) );
        }
    
    // TODO more here
    
    protected void addTriples( NodeToTriplesMap nt, String facts )
        {
        Triple [] t = tripleArray( facts );
        for (int i = 0; i < t.length; i += 1) nt.add( t[i] );
        }
    
    protected static Set just( Object x )
        {
        Set result = new HashSet();
        result.add( x );
        return result;
        }

    protected static Set both( Object x, Object y )
        {
        Set result = just( x );
        result.add( y );
        return result;
        }
    
    }


/*
	(c) Copyright 2004, 2005 Hewlett-Packard Development Company, LP
	All rights reserved.
	
	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions
	are met:
	
	1. Redistributions of source code must retain the above copyright
	   notice, this list of conditions and the following disclaimer.
	
	2. Redistributions in binary form must reproduce the above copyright
	   notice, this list of conditions and the following disclaimer in the
	   documentation and/or other materials provided with the distribution.
	
	3. The name of the author may not be used to endorse or promote products
	   derived from this software without specific prior written permission.
	
	THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
	OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
	IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
	DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
	THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/