package igrek.robopath.pathfinder.coopkilavuz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


/** 
 * <p>Utility class to parse command line arguments passed to main method.</p>
 * 
 * <p>For example, say these arguments are given:</p>
 * <pre>-verbose -out fileOut -in fileIn1 fileIn2</pre>
 * 
 * <ul>
 * <li><code>containsTag("-verbose")</code> returns true and consumes <i>-verbose</i></li>
 * <li><code>getArg("-verbose")</code> returns -out and consumes <i>-verbose</i> and <i>-out</i></li>
 * <li><code>getArg("-verbose", 1)</code> returns fileOut and consumes <i>-verbose</i> and <i>fileout</i></li>
 * <li><code>getArg("-out")</code> returns fileOut and consumes <i>-out</i> and <i>fileOut</i></li>
 * <li><code>getArg("-in")</code> returns fileIn1 and consumes <i>-in</i> and <i>fileIn1</i></li>
 * <li><code>getArg("-in",1)</code> returns fileIn2 and consumes <i>-in</i> and <i>fileIn2</i></li>
 * <li><code>getArg("-in", 2)</code> throws NoSuchElementException</li>
 * </ul>
 *  
 * @author hakan eryargi (r a f t)
 */
public class ComLineArgs implements Serializable {
	private static final long serialVersionUID = 1L;
	
    private String[] args = null;
    private List<String> argList = null; // map not used since it is not ordered.
    private List<String> unConsumed = null;
    
    /** 
     * Creates a new ComLineArgs.
     *  
     * @param args the arguments 
     * @param offset offset
     * @param length length 
     * */
    public ComLineArgs(String[] args, int offset, int length) {
        this.argList = new ArrayList<String>(length); 
        for (int i = 0; i < length; i++)
        	argList.add(args[offset + i]);
        this.unConsumed = new ArrayList<String>(argList);
        this.args = new String[length];
        System.arraycopy(args, offset, this.args, 0, length);
    }
    
    /** 
     * Creates a new ComLineArgs.
     *  
     * @param args the arguments. all of them will be used 
     * */
    public ComLineArgs(String[] args) {
        this(args, 0, args.length);
    }
    
    /** 
     * Creates a new ComLineArgs.
     *  
     * @param args the arguments. last (args.length - offset) arguments will be used 
     * @param offset offset
     * */
    public ComLineArgs(String[] args, int offset) {
        this(args, offset, args.length - offset);
    }
    
    /** Returns true if empty. */
    public boolean isEmpty() { return argList.isEmpty(); }
    
    /** Returns true if contains given tag. IE: any of the arguments equals to given tag. 
     * This method consumes the tag. */
    public boolean containsArg(String tag) {
        unConsumed.remove(tag);
        return argList.contains(tag);
    }
    
    /** returns index'th argument */
    public String getArg(int index) throws NoSuchElementException {
        if (args.length <= index)
            throw new NoSuchElementException("index: " + index);
        
        String result = args[index];
        unConsumed.remove(result);
        return result;
    }
    
    /** 
     * Returns index'th argument for tag. 
     * This method consumes both the tag and the index'th element of tag. 
     * 
     * @throws NoSuchElementException if tag or index'th element does not exist 
     * */
    public String getArg(String tag, int index) throws NoSuchElementException {
        if (! argList.contains(tag))
            throw new NoSuchElementException("tag [" + tag + "] not found");
        
        int tagIndex = argList.indexOf(tag);
        if (argList.size() > tagIndex + index + 1) {
            String result = argList.get(tagIndex + index + 1);
            unConsumed.remove(tag);
            unConsumed.remove(result);
            return result;
        }  else {
            throw new NoSuchElementException("argument [" + index + "] for [" + tag + "] not found.");
        }
    }
    
    /** 
     * Returns first argument for tag. Same as <code>getArg(tag, 0).</code>
     * This method consumes the first element of tag. 
     * */
    public String getArg(String tag) throws NoSuchElementException {
        return getArg(tag, 0);
    }
    
    /** returns true still exist any unconsumed argument */
    public boolean isUnconsumed() {
        return (! unConsumed.isEmpty());
    }
    
    /** returns the unconsumed arguments */
    public List<String> getUnconsumed() {
        return unConsumed;
    }
    
    /** returns the original arguments. */
    public String[] getArgs() {
        return args;
    }
}
