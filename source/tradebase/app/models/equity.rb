class Equity < ActiveRecord::Base
  has_many :dividends
  belongs_to :m_symbol
  
  has_many :trades, :as => :tradeable

  validates_uniqueness_of :m_symbol_id, :message => "Equity with that symbol already exists"
  
  def validate
    errors.add(:symbol, "Symbol cannot be empty") unless !m_symbol_root.blank?
  end

  # Returns the equity for the underlying root symbol, or creates a new one if it's not there
  def Equity.get_equity(ref_symbol, create_missing=true)
    symbol = MSymbol.find(:first, :conditions=>["root = ?", ref_symbol])
    if(symbol.nil?)
      if(create_missing)
        symbol = MSymbol.new(:root => ref_symbol)
        equity = Equity.new(:m_symbol => symbol)
        return equity
      else 
        return nil 
      end
    else
      return Equity.find_by_m_symbol_id(symbol.id)
    end
  end

  def to_s
    return "[" + self.m_symbol_root + " Equity]"
  end
  
  def m_symbol_root
    (self.m_symbol.nil?) ? nil : self.m_symbol.root
  end
  
end

